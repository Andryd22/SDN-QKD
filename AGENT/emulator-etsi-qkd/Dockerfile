FROM ubuntu:18.04

RUN apt-get update && apt-get upgrade -y \
    && apt-get install software-properties-common build-essential openssl libsystemd-dev libssl-dev libpcre2-dev git make cmake bison flex pkg-config graphviz doxygen valgrind zlib1g zlib1g-dev libev-dev libavl-dev libprotobuf-c-dev protobuf-c-compiler swig python-dev lua5.2 vim net-tools iputils-ping nano libcurl4-openssl-dev lib32ncurses5-dev man -y \
    && echo "export LD_LIBRARY_PATH=/lib:/usr/lib:/usr/local/lib" >> ~/.bashrc \
    && export LD_LIBRARY_PATH=/lib:/usr/lib:/usr/local/lib

RUN cd ~ && git clone http://git.libssh.org/projects/libssh.git \
    && export LD_LIBRARY_PATH=/lib:/usr/lib:/usr/local/lib \
    && cd libssh && mkdir build && cd build \
    && git checkout stable-0.8 \
    && cmake .. -DCMAKE_BUILD_TYPE=MinSizeRel \ 
    && make -j4 && make install

RUN cd ~ && git clone https://git.cryptomilk.org/projects/cmocka.git \
    && export LD_LIBRARY_PATH=/lib:/usr/lib:/usr/local/lib \
    && cd cmocka && mkdir build && cd build \
    && git checkout tags/cmocka-1.0.1 \
    && cmake .. -DCMAKE_BUILD_TYPE=MinSizeRel \ 
    && make && make install

RUN cd ~ && git clone https://github.com/CESNET/libyang \
    && export LD_LIBRARY_PATH=/lib:/usr/lib:/usr/local/lib \
    && cd libyang && git checkout v2.1.4 \
    && mkdir build && cd build \
    && cmake .. -DCMAKE_BUILD_TYPE=MinSizeRel \
    && make -j4 && make install

RUN cd ~ && git clone https://github.com/CESNET/libnetconf2.git \
    && export LD_LIBRARY_PATH=/lib:/usr/lib:/usr/local/lib \
    && cd libnetconf2 && git checkout v2.1.25 \
    && mkdir build && cd build \ 
    && cmake -DCMAKE_BUILD_TYPE=MinSizeRel -DENABLE_TLS=ON -DENABLE_SSH=ON .. \
    && make -j4 && make install

RUN cd ~ && git clone https://github.com/sysrepo/sysrepo.git \
    && export LD_LIBRARY_PATH=/lib:/usr/lib:/usr/local/lib

# new application changes file

RUN cd ~/sysrepo && git checkout v2.2.12

COPY config/application_changes_sarosh.c /root/sysrepo/examples/application_changes_example.c

RUN cd ~/sysrepo && mkdir build && cd build \
    && cmake -DCMAKE_BUILD_TYPE=Release -DCMAKE_INSTALL_PREFIX:PATH=/usr .. \
    && make -j4 \
    && ctest && make install

RUN cd ~ && git clone https://github.com/CESNET/Netopeer2.git \
    && export LD_LIBRARY_PATH=/lib:/usr/lib:/usr/local/lib \
    && cd Netopeer2 && git checkout v2.1.42 \
    && mkdir build && cd build \
    && cmake -DCMAKE_BUILD_TYPE=MinSizeRel .. \
    && make -j4 && make install

# install netconf-console2
RUN apt install python3-pip -y \
    && python3 -m pip install --upgrade pip setuptools wheel \
    && pip3 install netconf-console2

RUN cd ~ && ssh-keygen -t rsa -b 2048 -f /etc/ssh/ssh_host_rsa_key \
    && sh -c 'echo root:root | chpasswd' 

COPY yang /root/yang
COPY config /root/config
COPY script /root/script
COPY python_rest /root/python_rest

#EXPOSE 830
#EXPOSE 22
#EXPOSE 8080
WORKDIR /root
ENTRYPOINT ["sh", "/root/script/push-data.sh"]


