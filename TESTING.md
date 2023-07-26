# How to test

The plugin can be tested by using Docker containers.

Compile & make the plugin available via HTTP port 8011 on your local machine:

```bash
~ mvn -DskipTests clean package && \
podman run -p8011:80 -v/home/stephan/git/github/go-gchat-notifier-plugin/target/go-gchat-notifier-plugin.jar:/usr/share/nginx/html/go-gchat-notifier-plugin.jar nginx:1.19.9
```

Start up GoCD with TCP 8153 and let it download the plugin from the nginx above:

```bash
~ podman run -eGOCD_PLUGIN_INSTALL_a-plugin=http://$YOUR_IP:8011/go-gchat-notifier-plugin.jar -p8153:8153 gocd/gocd-server:v23.2.0
```

`$YOUR_IP` needs to be replaced with an IP of your host system.
