## Setup Addons Dashboard, Patient Portal and API

* Download all the config files from [here](https://drive.google.com/drive/folders/19PRs3iNRdmFlyMedmGLYs-khlOAIQ5jr?usp=sharing) 

* Install redis server ([Mac](https://formulae.brew.sh/formula/redis) | [Windows via Docker](https://hub.docker.com/_/redis) | [Ubuntu](https://launchpad.net/~chris-lea/+archive/ubuntu/redis-server))

* Install MySQL 5.7 ([Download](https://dev.mysql.com/downloads/mysql/5.7.html))

* Open mysql client and run the following queries

```sql
CREATE USER 'addon'@'localhost' IDENTIFIED BY 'addon';
GRANT ALL PRIVILEGES ON *.* TO 'addon'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;
```

* Create `~/.netrc` with the following contents

```
machine github.com
login <your github handle>
password <put personal token here>

machine api.github.com
login <your github handle>
password <put personal token here>
```
Ensure you set permission of `~/.netrc` to 0600
 
* Clone addon-api git repository `git clone git@github.com:practo/addon-api.git`

* Copy `addonsapi.config.json` to `addon-api/config/config.json`

* Run the following in addon-api Git repository

```sh
nvm use 8.9.4
npm install
npm run dev
```

* Clone partner-addons Git repository `git clone git@github.com:practo/partner-addons.git`

* Copy `dashboard.config.js` to `partner-addons/dashboard/config/config.js`

* Modify the url 'https://addons.practo.com' in widget.js and widgetDataHelper.js to 'http://addons.practo.local'

* Run the following in partner-addons Git repository

```sh
cd dashboard
nvm use 8.9.4
npm install
npm run build:dev
npm run start:dev
```
* Copy `portal.config.js` to `partner-addons/patient-portal/config/config.js`

* Run the following in partner-addons Git repository

```sh
cd patient-portal
nvm use 8.9.4
npm install
npm run start:dev
```

* Clone practoNav git repository `git clone git@github.com:practo/practoNav.git`

* Install nginx ([Mac](https://formulae.brew.sh/formula/nginx) | [Windows](http://nginx.org/en/download.html) | [Ubuntu](https://launchpad.net/~nginx/+archive/ubuntu/stable))

* Copy `proxy_params` to `nginx/proxy_params`

* Copy `schema_fix` to `nginx/scheme_fix`

* Copy `practodev.conf` to `nginx/sites-enabled/practodev`. Copy `test.html` to a your home folder. open `nginx/sites-enabled/practodev` and update `/home/ubuntu` and `/home/ubuntu/git` with respoective folder path.

* Edit `nginx/nginx.conf` and add following entry in http context section 

```nginx
underscores_in_headers on;
include scheme_fix;
log_format lt-custom '$remote_addr - $remote_user [$time_local]  '
                     '"$request" $status $body_bytes_sent '
                     '"$http_referer" "$http_user_agent" $request_time $http_request_handler_key';
```

* restart nginx

* Add following entries to `/etc/hosts`

```sh
127.0.0.1 accounts.practo.local nav.practo.local communicator.practo.local addons.practo.local api.insta.local www.widgetsite.local
```

* The nginx configuration allows access to accounts and communicator from Perfomance DB Test Server (172.16.18.48). Ensure Office VPN is reachable. 

* `www.widgetsite.local` is a local test site configured in nginx which supports only one resource `test.html`. You can create a file and map it to folder as configured in this server context, to host a local widget for testing.

* Open http://addons.practo.local/dashboard


### Create an Account in Accounts Local

* provide phone number, password and click signup

* As SMS provider is not configured run the following command to obtain OTP from DynamoDB. Ensure Office VPN is reachable.

```sh
ssh root@172.16.18.48 /root/get_otp.sh {E164 Phone Number}
```
