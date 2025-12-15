#!/bin/bash

# Termino immediatamente lo script se un qualsiasi comando fallisce.
# per evitare che il container parta in uno stato "rotto".
set -e

# Avvio e Configurazione di RabbitMQ

echo "--> Avvio del server RabbitMQ in background..."
rabbitmq-server &

# Attendeo che il server sia completamente avviato prima di procedere.
echo "--> Attesa che RabbitMQ sia pronto..."
rabbitmqctl wait /var/lib/rabbitmq/mnesia/rabbit@$(hostname).pid --timeout 60

echo "--> Configurazione di RabbitMQ..."

# Controllo se l'utente 'node' esiste già. Se non esiste, lo creo.
if ! rabbitmqctl list_users | grep -q "^node"; then
    echo "L'utente 'node' non esiste. Lo creo..."
    rabbitmqctl add_user node node
else
    echo "L'utente 'node' esiste già, salto la creazione."
fi

# Imposto tag e permessi.
rabbitmqctl set_user_tags node administrator
rabbitmqctl set_permissions -p / node ".*" ".*" ".*"
echo "--> Configurazione di RabbitMQ completata."


# --- Avvio del server SSH ---

# Avvia SSH in primo piano. Questo comando è l'ultimo e tiene il container in esecuzione.
echo "--> Avvio del server SSH..."
/usr/sbin/sshd -D
