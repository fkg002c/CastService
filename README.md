# CastService
Simple cast service

build
install: adb install app\build\outputs\apk\debug\app-debug.apk
start: adb shell am {startservice | start-foreground-service} com.fkg002c.apps.castservice.START
!!! check status area: microphone permission may be required
stop: adb shell am {startservice | start-foreground-service} com.fkg002c.apps.castservice.STOP
uninstall: adn uninstall com.fkg002c.apps.castservice


HOST sample as part of Thread based class

    static int sampleRate = 16000;
    static SourceDataLine sourceDataLine;

    /**
     * Receive Socket's InputStream
     */
    @Override
    public void run() {
        super.run();
        try {
            ServerSocket serverSocket = new ServerSocket(port);

            while (!Thread.interrupted() && AndroidListener.adbDeviceInfo.get(serialNo).isRunFlag()) {
                Socket socket = serverSocket.accept();
                byte[] receiveData = new byte[2560];

                AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
                DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
                sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                sourceDataLine.open(format);
                sourceDataLine.start();

                FloatControl volumeControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
                volumeControl.setValue(1.00f);

                InputStream in = socket.getInputStream();
                int len = 0;
                while ((len = in.read(receiveData)) != -1) {
                    // ByteArrayInputStream baiss = new ByteArrayInputStream(receiveData);
                    // AudioInputStream ais = new AudioInputStream(baiss, format, len);
                    toSpeaker(receiveData);
                }
                in.close();
                socket.close();

                sourceDataLine.drain();
                sourceDataLine.close();
            }
            serverSocket.close();
        } catch (LineUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void toSpeaker(byte soundbytes[]) {
        try {
            sourceDataLine.write(soundbytes, 0, soundbytes.length);
        } catch (Exception e) {
            System.out.println("Not working in speakers...");
            e.printStackTrace();
        }
    }
