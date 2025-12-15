import socket
import sys
import json
import struct 
import os
import threading
import time

HOST = "127.0.0.1"  # Standard loopback interface address (localhost)
SOCKET_PORT = int(os.getenv('SOCKET_PORT'))


def remap_attributes(param_name):
    switcher = {
        "phys_wavelength": "wavelength",
        "phys_channel_att":"channel_att" 
    }
    return switcher.get(param_name, "no_correspondence")

PORT_QKD_NODE = int(os.getenv('PORT_QKD_NODE'))

PORT_OTHER_QKD_NODE = int(os.getenv('PORT_OTHER_QKD_NODE'))




def communicate_link_id(link_id,):
    json_msg = json.dumps({'command': "set_attribute", 'attribute': 'link_id', 'value': link_id})
    # sending message
    clientSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    # need to add : --add-host=host.docker.internal:host-gateway to docker run
                
    clientSocket.connect(("host.docker.internal",PORT_QKD_NODE))

    # TRASMISSIONE
    # Lunghezza del vero messaggio:
    clientSocket.sendall(struct.pack('>I',len(json_msg)))
    # Vero messaggio:
    clientSocket.sendall(str(json_msg).encode())

    # RICEZIONE RISPOSTA
    # Lunghezza della risposta:
    bufpack = clientSocket.recv(4)
    # Vera risposta:
    buf = struct.unpack('>I',bufpack)[0]
    data = clientSocket.recv(buf)
    data_deserialized = data.decode('utf8').replace("'", '"')
    with open('output_rest_server.txt', 'a') as sys.stdout:
        print('Sent link id to Alice: ',link_id)
        print(data_deserialized)
        try:
            received_data_from_device = json.loads(data_deserialized)
            print("[{}]".format(received_data_from_device))
        except:
            print("Empty answer")
    clientSocket.close()

    clientSocket_other = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    clientSocket_other.connect(("host.docker.internal",PORT_OTHER_QKD_NODE))

    # TRASMISSIONE
    # Lunghezza del vero messaggio:
    clientSocket_other.sendall(struct.pack('>I',len(json_msg)))
    # Vero messaggio:
    clientSocket_other.sendall(str(json_msg).encode())

    # RICEZIONE RISPOSTA
    # Lunghezza della risposta:
    bufpack = clientSocket_other.recv(4)
    # Vera risposta:
    buf = struct.unpack('>I',bufpack)[0]
    data = clientSocket_other.recv(buf)
    data_deserialized = data.decode('utf8').replace("'", '"')
    with open('output_rest_server.txt', 'a') as sys.stdout:
        print(data_deserialized)
        try:
            received_data_from_device = json.loads(data_deserialized)
            print("[{}]".format(received_data_from_device))
        except:
            print("Empty answer")
    clientSocket_other.close()


def send_message(json_msg):
    # sending message
    clientSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    # need to add : --add-host=host.docker.internal:host-gateway to docker run
    
    clientSocket.connect(("host.docker.internal",PORT_QKD_NODE))

    # TRASMISSIONE
    # Lunghezza del vero messaggio:
    clientSocket.sendall(struct.pack('>I',len(json_msg)))
    # Vero messaggio:
    clientSocket.sendall(str(json_msg).encode())

    # RICEZIONE RISPOSTA
    # Lunghezza della risposta:
    bufpack = clientSocket.recv(4)
    # Vera risposta:
    buf = struct.unpack('>I',bufpack)[0]
    data = clientSocket.recv(buf)
    data_deserialized = data.decode('utf8').replace("'", '"')
    print(data_deserialized)
    try:
        received_data_from_device = json.loads(data_deserialized)
        print("[{}]".format(received_data_from_device))
    except:
        print("Empty answer") 
    clientSocket.close()

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.bind((HOST, SOCKET_PORT))
    s.listen()
    while True:
        conn, addr = s.accept()
        with conn:
            data = conn.recv(1024).decode() # decode to remove the leading b (byte)
            with open('output_rest_server.txt', 'a') as sys.stdout:

                string_to_parse = str(data)

                string_to_parse = string_to_parse.split(",")

        
                #print(string_to_parse[0]) # parameter_name: name
                param_name = string_to_parse[0].split("/")[-1]

                if "qkdl_status" in param_name:
                    # link status has changed
                    #parameter_name:/etsi-qkd-sdn-node:qkd_node/qkd_links/qkd_link[qkdl_id='f81d4fae-aaaa-aaaa-aaaa-00a0c91e6bf2']/qkdl_status,
                    # old_value:etsi-qkd-node-types:OFF
                    # ,new_value:etsi-qkd-node-types:ACTIVE
                    string_to_parse = str(data).split(",")
                    
                    old_value = string_to_parse[1].split(":")[2]
                    print("OLD VALUE IS: ",old_value)
                    new_value = string_to_parse[2].split(":")[2]
                    if "OFF" in old_value:
                        # the change of the status is to active
                        print("Status changed to active")
                        cmd = "Start from external"

                    else:
                        # the change of the status is to inactive
                        print("Status changed to inactive")
                        cmd = "Stop from external"
                    json_msg = json.dumps({'command': cmd, 'attribute': param_name, 'value': new_value})
                    #print(string_to_parse[2]) # new_value:value
                    send_message(json_msg)


                elif "qkdl_id" in param_name:
                    # new link id is created
                    # string received is: qkd_link[qkdl_id='f81d4fae-aaaa-aaaa-aaaa-00a0c91e6bf4']
                    tmp_1 = param_name.split("[")[1]
                    tmp_2 = tmp_1.split("=")
                    param_name = tmp_2[0]
                    new_value = tmp_2[1].split(']')[0].strip('\'')
                    print(new_value)
                    json_msg = json.dumps({'command': "Connect from external", 'attribute': param_name, 'value': new_value})
                    # start thread after 2 seconds to send both A and B the linkid
                    send_message(json_msg)
                    time.sleep(5)
                    t = threading.Thread(target=communicate_link_id,args=(new_value,))
                    t.start()
                    
                
            #conn.sendall(data)
