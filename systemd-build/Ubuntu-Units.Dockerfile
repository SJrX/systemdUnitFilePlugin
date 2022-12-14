FROM ubuntu:22.04

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && apt-get -y install apt-file && apt-file update

ADD ubuntu-units.sh /

# Build a cache in the image
RUN /ubuntu-units.sh > /ubuntu-units.txt

CMD /ubuntu-units.sh > /mount/ubuntu-units.txt && chmod 777 /mount/ubuntu-units.txt
