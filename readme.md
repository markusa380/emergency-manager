# Emergency Manager

![Scala CI](https://github.com/markusa380/emergency-manager/workflows/Scala%20CI/badge.svg?branch=master)


## Server Setup on Amazon AWS

### Prerequisites:
* Amazon EC2 instance
    * Running Amazon Linux 2
    * In a Public VPC
    * Attached IAM role to access DynamoDB
    * Attached security group to allow ports 22, 80, 443
* Route 53 registered DNS pointing to EC2 instance

### Steps:

* Update `yum`
```
sudo yum update -y
```

* Install Java

```
sudo yum install java-1.8.0
```

* Install `httpd`
```
sudo yum install -y httpd
```

* Configure `httpd`
```
sudo systemctl start httpd
sudo systemctl enable httpd
sudo usermod -a -G apache ec2-user
```

* Install `mod_ssl`
```
sudo yum install -y mod_ssl
```

* Navigate to `~`

* Install `certbot` repo

```
sudo wget -r --no-parent -A 'epel-release-*.rpm' http://dl.fedoraproject.org/pub/epel/7/x86_64/Packages/e/

sudo rpm -Uvh dl.fedoraproject.org/pub/epel/7/x86_64/Packages/e/epel-release-*.rpm

sudo yum-config-manager --enable epel*
```

* Edit the main Apache configuration file, `/etc/httpd/conf/httpd.conf`. Locate the `Listen 80` directive and add the following lines after it, replacing the example domain names.

```
<VirtualHost *:80>
    DocumentRoot "/var/www/html"
    ServerName "example.com"
    ServerAlias "www.example.com"
</VirtualHost>
```

* Restart `httpd`
```
sudo systemctl restart httpd
```

* Install `certbot`
```
sudo yum install -y certbot python2-certbot-apache
```

* Run `certbot`
```
sudo certbot
```

* Open `/etc/crontab` and add the following line

```
39      1,13    *       *       *       root    certbot renew --no-self-upgrade
```

* Restart `cron`

```
sudo systemctl restart crond
```

* Copy the following files to the EC2 instance:

    * `backend/src/main/bash/backend.sh` &rarr; `~/backend.sh`
    * `frontend/target/scala-2.13/assets/*` &rarr; `~/*`
    * `backend/target/scala-2.13/backend.jar` &rarr; `~/backend.jar`

* Open `/etc/httpd/conf/httpd.conf` again and add the entry, replacing the example domain names.

```
<VirtualHost *:443>
    ServerName aws-web-test.de
    ServerAlias www.aws-web-test.de
    ProxyPreserveHost On
    ProxyRequests off
    AllowEncodedSlashes NoDecode

    <Proxy *>
          Order deny,allow
          Allow from all

    </Proxy>

    SSLEngine on
    SSLCertificateFile /etc/letsencrypt/live/aws-web-test.de/fullchain.pem
    SSLCertificateKeyFile /etc/letsencrypt/live/aws-web-test.de/privkey.pem
    Include /etc/letsencrypt/options-ssl-apache.conf

    ProxyPass / http://localhost:8080/ nocanon
    ProxyPassReverse / http://localhost:8080/

    RequestHeader set X-Forwarded-Proto "https"
    RequestHeader set X-Forwarded-Port "443"
</VirtualHost>
```

* Restart `httpd`
```
sudo systemctl restart httpd
```

