import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.net.InetAddress;
import java.util.regex.*;

/**
 * Created by Admin on 29.11.2016.
 */
public class ChatUDP extends JFrame{

    private  JTextArea taMain;
    private JTextField tfMq;

    private final String FRM_TITLE = "Our Tiny Chat";
    private final int FRM_LOC_X = 100;
    private final int FRM_LOC_Y = 100;
    private final int FRM_WIDTH = 400;
    private final int FRM_HEIGHT = 400;

    private final int PORT = 9876;
    private final String IP_BROADCAST = "192.168.0.101"; //попробовать InetAddress.getLocalHost()

    private class theReceiver extends Thread{
        @Override
        public synchronized void start() {
            super.start();
            // организуем прием - серверную часть
            try {
                customize();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        private void customize() throws Exception{
            //открываем сокет на конкретный порт
            DatagramSocket receiveSocket = new DatagramSocket(PORT);
            //чтобы отсечь лишнее в тексте используем регул¤пки
            Pattern regex  = Pattern.compile("[\u0020-\uFFFF]");//выведем все видимые дл¤ ”“‘-8 символы отсека¤ первые 20 табул¤ци¤ и тд.

            //бесконечно ждем
            while (true){
                byte[] receiveData = new byte[1024];//килобайтной длины пакета пока хватит
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                receiveSocket.receive(receivePacket);
                //подсмотрим адрес отправител¤ в”“ѕ пакете
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();

                String sentence = new String (receivePacket.getData());
                Matcher m = regex.matcher(sentence);

                taMain.append(IPAddress.toString() + ":"+port+": "); //перед сообщением добавим адрес и порт
                while (m.find())
                    taMain.append(sentence.substring(m.start(), m.end()));
                taMain.append("\r\n");//добавим переход на новую строку
                taMain.setCaretPosition(taMain.getText().length());//каретку ставим в начало, чтобы новые строки не уходили вних
            }
        };
    }

    private void antistatic(){
        framDraw(new ChatUDP());
        new theReceiver().start();
    }

    //метод отливливающий исключени¤ в строку
    //открываем в нем сокет, формируем пакет, отправить в широкое вещание
    //при каждом вызове создаетс¤ новый массив
    private void btnSend_Handler()throws Exception{
        DatagramSocket sendSocket = new DatagramSocket();
        InetAddress IPAdress = InetAddress.getByName(IP_BROADCAST);//распарсить строку в IP адрес
        byte[] sendDate; //массив из байтов
        String sentense = tfMq.getText(); //читаем текст из строки
        tfMq.setText(""); //чтобы не флудить просто клика¤ на кнопку
        sendDate = sentense.getBytes("UTF-8"); // стандартна¤ кодировка дл¤ мультисистем - линус, мобилки, вин...

        DatagramPacket sendPacket = new DatagramPacket(sendDate, sendDate.length, IPAdress, PORT); //передаем в пакет данные + длину пакету + айпи + порт получател¤
        sendSocket.send(sendPacket);
    }

    private void framDraw(JFrame frame) {
        tfMq = new JTextField();
        taMain = new JTextArea(FRM_HEIGHT/19, 50);
        JScrollPane spMain  = new JScrollPane(taMain);//колесо прокрутки, передаемобъект где примен¤етс¤
        spMain.setLocation(0,0);
        taMain.setLineWrap(true); //переносы
        taMain.setEditable(false); //отключаем редактирование окна вывода

        JButton btnSend = new JButton();
        btnSend.setText("Send");
        btnSend.setToolTipText("Broadcast a message");
        // событие по клику на кнопку с применением л¤мюда выражени¤
        btnSend.addActionListener(e -> {
            try {
                btnSend_Handler();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); //стандартное закрытие
        frame.setTitle(FRM_TITLE);
        frame.setLocation(FRM_LOC_X, FRM_LOC_Y);
        frame.setSize(FRM_WIDTH,FRM_HEIGHT);
        frame.setResizable(false); //запретить измен¤ть размер
        frame.getContentPane().add(BorderLayout.NORTH, spMain); //на верху, «џ можно поробовать просто frame.add
        frame.getContentPane().add(BorderLayout.CENTER, tfMq); //по центру
        frame.getContentPane().add(BorderLayout.EAST, btnSend); //на востоке
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new ChatUDP().antistatic();
    }
}
