## How to build APIDocs

#### One time install (OSX)

```sh
brew tap bukalapak/packages
brew install snowboard
```

#### One time install (ubuntu)

```sh
wget https://github.com/bukalapak/snowboard/releases/download/v1.6.1/snowboard-v1.6.1.linux-amd64.tar.gz
tar -zxvf snowboard-v1.6.1.linux-amd64.tar.gz
sudo mv snowboard /usr/bin 
```

#### To build docs

```sh
snowboard html -o $INSTAHMS_BASE/help/apidocs.html -t $INSTAHMS_BASE/apidocs/apidoc_template.html $INSTAHMS_BASE/apidocs/main.apib
```

#### Dev build with watch

```sh
snowboard -w html -o $INSTAHMS_BASE/help/apidocs.html -t $INSTAHMS_BASE/apidocs/apidoc_template.html -s $INSTAHMS_BASE/apidocs/main.apib
```

Access doc on http:///localhost:8088
