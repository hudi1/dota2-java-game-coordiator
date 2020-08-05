```java
    public static void main(String[] args) throws Exception {
        // logback
        // System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "src/main/resources/dota-gc-logback.xml");

        SteamClientConfig config = new SteamClientConfig();
        config.setPass("pass");
        config.setUser("user");
        // optional
        // config.setSteamWebApi("webapi");
        // optional to remember user
        config.setLoginKey("loginkey.txt");
        config.setSentry("sentry.bin");
        // after first failed login because of TwoFactor or SteamGuard set one of this property
        // config.setAuthCode("authCode");
        // config.setTwoFactorAuth("factorAuth");
        Dota2Client client = new Dota2Client(config);
        client.connect();
        // wait till connected or when client.isReady()
        Thread.sleep(10000);

        // Print notail profile card
        System.out.println(client.getPlayerHandler().requestProfileCard(19672354));
    }
 ```    
