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
    private final String IP_BROADCAST = "192.168.0.101"; //����������� InetAddress.getLocalHost()

    private class theReceiver extends Thread{
        @Override
        public synchronized void start() {
            super.start();
            // ���������� ����� - ��������� �����
            try {
                customize();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        private void customize() throws Exception{
            //��������� ����� �� ���������� ����
            DatagramSocket receiveSocket = new DatagramSocket(PORT);
            //����� ������ ������ � ������ ���������� ���������
            Pattern regex  = Pattern.compile("[\u0020-\uFFFF]");//������� ��� ������� ��� ���-8 ������� ������� ������ 20 ��������� � ��.

            //���������� ����
            while (true){
                byte[] receiveData = new byte[1024];//����������� ����� ������ ���� ������
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                receiveSocket.receive(receivePacket);
                //���������� ����� ����������� ���� ������
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();

                String sentence = new String (receivePacket.getData());
                Matcher m = regex.matcher(sentence);

                taMain.append(IPAddress.toString() + ":"+port+": "); //����� ���������� ������� ����� � ����
                while (m.find())
                    taMain.append(sentence.substring(m.start(), m.end()));
                taMain.append("\r\n");//������� ������� �� ����� ������
                taMain.setCaretPosition(taMain.getText().length());//������� ������ � ������, ����� ����� ������ �� ������� ����
            }
        };
    }

    private void antistatic(){
        framDraw(new ChatUDP());
        new theReceiver().start();
    }

    //����� ������������� ���������� � ������
    //��������� � ��� �����, ��������� �����, ��������� � ������� �������
    //��� ������ ������ ��������� ����� ������
    private void btnSend_Handler()throws Exception{
        DatagramSocket sendSocket = new DatagramSocket();
        InetAddress IPAdress = InetAddress.getByName(IP_BROADCAST);//���������� ������ � IP �����
        byte[] sendDate; //������ �� ������
        String sentense = tfMq.getText(); //������ ����� �� ������
        tfMq.setText(""); //����� �� ������� ������ ������ �� ������
        sendDate = sentense.getBytes("UTF-8"); // ����������� ��������� ��� ������������ - �����, �������, ���...

        DatagramPacket sendPacket = new DatagramPacket(sendDate, sendDate.length, IPAdress, PORT); //�������� � ����� ������ + ����� ������ + ���� + ���� ����������
        sendSocket.send(sendPacket);
    }

    private void framDraw(JFrame frame) {
        tfMq = new JTextField();
        taMain = new JTextArea(FRM_HEIGHT/19, 50);
        JScrollPane spMain  = new JScrollPane(taMain);//������ ���������, �������������� ��� �����������
        spMain.setLocation(0,0);
        taMain.setLineWrap(true); //��������
        taMain.setEditable(false); //��������� �������������� ���� ������

        JButton btnSend = new JButton();
        btnSend.setText("Send");
        btnSend.setToolTipText("Broadcast a message");
        // ������� �� ����� �� ������ � ����������� ������ ���������
        btnSend.addActionListener(e -> {
            try {
                btnSend_Handler();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); //����������� ��������
        frame.setTitle(FRM_TITLE);
        frame.setLocation(FRM_LOC_X, FRM_LOC_Y);
        frame.setSize(FRM_WIDTH,FRM_HEIGHT);
        frame.setResizable(false); //��������� �������� ������
        frame.getContentPane().add(BorderLayout.NORTH, spMain); //�� �����, �� ����� ���������� ������ frame.add
        frame.getContentPane().add(BorderLayout.CENTER, tfMq); //�� ������
        frame.getContentPane().add(BorderLayout.EAST, btnSend); //�� �������
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new ChatUDP().antistatic();
    }
}
