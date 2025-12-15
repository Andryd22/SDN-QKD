import socket
import json
import xml.etree.ElementTree as ET
import re
import struct
import subprocess
import time
# --- NUOVO ---
import os
import requests
import ast
# --- FINE NUOVO ---

HOST = '0.0.0.0'
PORT = 5055

SYSREPO_HOST = '127.0.0.1'
SYSREPO_PORT = '830'

# --- NUOVO: Logica e variabili per Quditto ---
def log(msg):
    print(msg, flush=True)
    with open("event_log.txt", "a", encoding="utf-8") as f:
        f.write(msg + '\n')

QUDITTO_IP = os.getenv('QUDITTO_IP')
QUDITTO_PORT = os.getenv('QUDITTO_PORT', '8000')

if QUDITTO_IP:
    QUDITTO_BASE_URL = f"http://{QUDITTO_IP}:{QUDITTO_PORT}/api/v1"
    log(f"[INFO] Agent configurato per parlare con Quditto su: {QUDITTO_BASE_URL}")
else:
    QUDITTO_BASE_URL = None
    log("[ERROR] QUDITTO_IP non impostata! L'agent non potrà contattare il nodo Quditto.")
# --- FINE NUOVO ---

# --- MODIFICA 1: Aggiunta mappatura ID ---
# Mappa gli ID ETSI (usati da ONOS) ai nomi dei nodi (usati da Quditto)
ETSI_TO_QUDITTO_MAP = {
    "aaaaaaaa-aaaa-aaaa-0031-000000006055": "A", 
    "aaaaaaaa-aaaa-aaaa-0032-000000006055": "B", 
    "aaaaaaaa-aaaa-aaaa-0033-000000006055": "C"  
}
# --- FINE MODIFICA 1 ---


SIGNATURE_ALICE_NAME = 'SUD'
SIGNATURE_ALICE_HOST = '10.30.2.26'
SIGNATURE_ALICE_PORT = 50000

SIGNATURE_BOB_NAME = 'SUD'
SIGNATURE_BOB_HOST = '10.30.2.26'
SIGNATURE_BOB_PORT = 55000


DATASTORE_READ = '/root/python_rest/icton25-get-snapshot.sh'
DATASTORE_FILE = 'config_snapshot.xml' 

# --------- Funzioni modificate per Quditto
def forward_signature(auth_sign, signature_host, signature_port):
    log(f"[DEBUG] Chiamata a forward_signature con {auth_sign}. Bypassata per il test.")
    return True 

def forward_qkd_connect(local_ip, target_ip_address):
    log(f"[INFO] forward_qkd_connect: Chiamata ricevuta per {target_ip_address}. (Nessuna azione richiesta per Quditto REST API).")
    pass
    
def forward_qkd_disconnect(local_ip):
    log(f"[INFO] forward_qkd_disconnect: Chiamata ricevuta. (Nessuna azione richiesta per Quditto REST API).")
    pass
    
# --- MODIFICA 2 ---
def forward_qkd_start(remote_sae_id, key_size=512):
    # Controllo preliminare
    if QUDITTO_BASE_URL is None:
        log("[ERROR] QUDITTO_IP non impostato. Impossibile chiamare forward_qkd_start.")
        return

    # Traduczione ID ETSI nel nome Quditto
    quditto_partner_name = ETSI_TO_QUDITTO_MAP.get(remote_sae_id)
    if not quditto_partner_name:
        log(f"[ERROR] ID ETSI non trovato in mappa: {remote_sae_id}")
        return
    
    
    log(f"[INFO] Tradotto ID ETSI {remote_sae_id} -> Nome Quditto {quditto_partner_name}")

    # Costruzione ed Esecuzione della Richiesta
    api_url = f"{QUDITTO_BASE_URL}/keys/{quditto_partner_name}/enc_keys"
    params = {'size': key_size}
    log(f"[INFO] Chiamata a Quditto (master): GET {api_url} con params={params}")
    try:
        response = requests.get(api_url, params=params, timeout=60)     # 60
        response.raise_for_status()
        data = response.json()      # Decodifica risposta
        log(f"[INFO] Risposta RAW da Quditto: {data}")
        if isinstance(data, (list, set)) and len(data) > 0:
            inner_json_str = list(data)[0]

            try:
                key_data = ast.literal_eval(inner_json_str)
            except (ValueError, SyntaxError):

                key_data = {"message": inner_json_str}

            key_id = key_data.get('key_ID')
            if key_id:      # Validazione
                log(f"[SUCCESS] Quditto ha avviato la generazione. Key_ID: {key_id}")
            else:
                log(f"[WARNING] Risposta OK da Quditto, ma nessun key_ID trovato: {key_data}")
        else:
            log(f"[WARNING] Risposta OK da Quditto, ma formato non riconosciuto: {data}")
    except Exception as e:
        log(f"[ERROR] Errore generico in forward_qkd_start: {e}")
# --- FINE MODIFICA 2 ---
    
def forward_qkd_stop(local_ip):
    log(f"[INFO] forward_qkd_stop: Chiamata ricevuta. (Nessuna azione richiesta per Quditto REST API).")
    pass    
    
# --------- Funzione update_link_status
def update_link_status(qkdl_id, enable, status, auth_status):
    try:
        subprocess.run(
            [
                '/root/python_rest/edit-link-status.sh', 
                qkdl_id, enable, status, auth_status,
                SYSREPO_HOST, SYSREPO_PORT
            ],
            stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=True
        )
    except Exception as e:
        log(f"[DEBUG] Error executing edit-link-status.sh: {e}")


# ---------------------------------------
# --- Main code with logic implementation
# ---------------------------------------
with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    log(f"[INFO] Listening to application_chages_example.c on socket {HOST}:{PORT}")
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind((HOST, PORT))
    s.listen()

    while True:
        conn, addr = s.accept()
        with conn:
            data = conn.recv(1024).decode('utf-8').strip()
            if not data:
                log(f"Empty message received")
                continue

            try:
                parsed = json.loads(data)
                parameter = parsed.get("parameter_name", "<unknown>")
                old = parsed.get("old_value", "<none>")
                new = parsed.get("new_value", "<none>")

                prefix = "/etsi-qkd-sdn-node:qkd_node"
                if parameter.startswith(prefix):
                    param = parameter[len(prefix):]
                else:
                    param = parameter

                log(f"[INFO] Event PARAM: {param} - OLD: {old} - NEW: {new}")

                # --- CORREZIONE ---
                match = re.search(r"/qkd_links/qkd_link\[qkdl_id='([^']+)'\]", param)
                # --- FINE CORREZIONE ---
                
                if match:
                    
                    subprocess.run(DATASTORE_READ) 
                    
                    qkdl_id = match.group(1)
                    try:
                        
                        tree = ET.parse(DATASTORE_FILE) 
                        root = tree.getroot()
                        ns = {'n': 'urn:etsi:qkd:yang:etsi-qkd-node'}
                        xpath = f".//n:qkd_links/n:qkd_link[n:qkdl_id='{qkdl_id}']" # Percorso corretto
                        link = root.find(xpath, ns)

                        if link is not None:
                            role_node = link.find('n:phys_qkd_role', ns)
                            status_node = link.find('n:qkdl_status', ns)
                            remote_node = link.find('n:qkdl_remote/n:qkdn_id', ns)

                            role = role_node.text if role_node is not None else "UNKNOWN"
                            status = status_node.text if status_node is not None else "UNKNOWN"
                            remote = remote_node.text if remote_node is not None else "UNKNOWN"
                            
                            if "qkdl_enable" in param:
                                if ("OFF" in status and old == "false" and new == "true"):
                                    log(f"[OK] Link ENABLE -> Link moved to PENDING state")
                                    update_link_status(qkdl_id, 'true', 'PENDING', 'PROGRESSING')
                                    continue
                                
                            if "qkdl_status" in param:
                                if ("PENDING" in new):
                                    log(f"[OK] New status is PENDING -> authenticating...")
                                    
                                    # --- BYPASS AUTENTICAZIONE ---
                                    authentication = True 
                                    log(f"[DEBUG] Authentication bypassed, setting to True.")
                                    # --- FINE BYPASS ---
                                    
                                    if (authentication):
                                        log(f"[OK] authentication SUCCESS")
                                        update_link_status(qkdl_id, 'true', 'ACTIVE', 'AUTHORIZED')
                                    else:
                                        log(f"[ERROR] authentication FAILED")
                                        update_link_status(qkdl_id, 'true', 'OFF', 'NOT_AUTHORIZED')
                                    continue
                                    
                                if ("ACTIVE" in new):
                                    log(f"[OK] New status is ACTIVE -> connecting with partner QKD-node {remote}")
                                    forward_qkd_connect(None, None) 
                                    if ("TRANSMITTER" in role):
                                        log(f"[OK] Role is TRANSMITTER -> starting keys generation with partner {remote}")
                                        forward_qkd_start(remote, 512) 
                                    continue
                                
                        else:
                            log(f"[ERROR] No link found with ID {qkdl_id} in snapshot.")
                    except ET.ParseError as e:
                        log(f"[ERROR] Failed to parse XML (ParseError): {e}")
                    except FileNotFoundError as e:
                        log(f"[ERROR] Failed to parse XML (FileNotFound): {e}") 
                    except Exception as e:
                        log(f"[ERROR] Failed to parse XML (General Exception): {e}")
                else:
                    log(f"[INFO] Event received for a parameter without qkdl_id (e.g., qkdn_id). Ignoring. Param: {param}")
            except json.JSONDecodeError as e:
                log(f"[ERROR] Failed to parse JSON: {e}")
