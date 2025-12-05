# Immagine Ubuntu di base
FROM ubuntu:22.04

# Evito richieste interattive durante l'installazione
ENV DEBIAN_FRONTEND=noninteractive

# Installo SSH, Python, sudo E RabbitMQ
RUN apt-get update && apt-get install -y --no-install-recommends \
    openssh-server \
    python3 \
    python3-pip \
    sudo \
    rabbitmq-server \
    net-tools \
    netcat-traditional \
    lsof \
    wget \ 
    nano \
    iputils-ping \
    telnet \
    vim \
    erlang-base \
    erlang-asn1 erlang-crypto erlang-eldap erlang-ftp erlang-inets \
    erlang-mnesia erlang-os-mon erlang-parsetools erlang-public-key \
    erlang-runtime-tools erlang-snmp erlang-ssl \
    erlang-syntax-tools erlang-tftp erlang-tools erlang-xmerl \
    curl gnupg apt-transport-https \
    && rm -rf /var/lib/apt/lists/*

# Installo i pacchetti PIP
RUN pip install fastapi "pydantic<2" uvicorn aio_pika pika PyYAML

# Installa pydynaa
RUN wget https://Andry:TesiQKD2025@pypi.netsquid.org/pydynaa/pydynaa-1.0.2-cp310-cp310-linux_x86_64.whl && \
    pip install pydynaa-1.0.2-cp310-cp310-linux_x86_64.whl

# Installa netsquid
RUN wget https://Andry:TesiQKD2025@pypi.netsquid.org/netsquid/netsquid-1.1.8-cp310-cp310-linux_x86_64.whl && \
    pip install netsquid-1.1.8-cp310-cp310-linux_x86_64.whl
    
# Creo l'utente 'ubuntu' con password 'ubuntu'
RUN useradd -m -s /bin/bash ubuntu 
RUN echo "ubuntu:ubuntu" | chpasswd 
RUN adduser ubuntu sudo

# Configuro il server SSH per permettere il login con password
RUN sed -i 's/#PasswordAuthentication yes/PasswordAuthentication yes/' /etc/ssh/sshd_config
RUN sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config
RUN mkdir /run/sshd

# Copio lo script di avvio nel container
COPY entrypoint.sh /usr/local/bin/entrypoint.sh

# Espongo la porta SSH
EXPOSE 22 

# Imposto lo script come comando di avvio principale
CMD ["/usr/local/bin/entrypoint.sh"]
