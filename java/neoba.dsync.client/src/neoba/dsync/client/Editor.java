package neoba.dsync.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import openvcdiffjava.Openvcdiffjava;

public class Editor extends javax.swing.JFrame {

    private final Selector selector;
    private final Charset charset = Charset.forName("UTF-8");
    private SocketChannel sc = null;
    private String dict = "";
    private Openvcdiffjava differ = new Openvcdiffjava();

    public Editor() throws IOException {
        initComponents();
        selector = Selector.open();
        InetSocketAddress isa = new InetSocketAddress("127.0.0.1", 3000);
        sc = SocketChannel.open(isa);
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_READ);
        new ClientThread().start();

    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        DocArea = new javax.swing.JTextArea();
        syncButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        DocArea.setColumns(20);
        DocArea.setRows(5);
        jScrollPane1.setViewportView(DocArea);

        syncButton.setText("Sync");
        syncButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syncButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Document: ");

        jLabel2.setText("Dictionary: ");

        jLabel3.setText("Neoba Dsync Client");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(135, 135, 135)
                        .addComponent(jLabel2)
                        .addGap(0, 135, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(syncButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(syncButton)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void syncButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syncButtonActionPerformed
        try {
            ByteBuffer buf = ByteBuffer.wrap(differ.vcdiffEncode(dict, DocArea.getText()));
            sc.write(buf);
        } catch (IOException ex) {
            Logger.getLogger(Editor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_syncButtonActionPerformed
    private class ClientThread extends Thread {

        @Override
        public void run() {
            try {
                while (selector.select() > 0) {
                    for (SelectionKey sk : selector.selectedKeys()) {
                        selector.selectedKeys().remove(sk);
                        if (sk.isReadable()) {
                            SocketChannel sc = (SocketChannel) sk.channel();
                            ByteBuffer buff = ByteBuffer.allocate(9000);
                            int count = 0, size = 0;
                            while ((size = sc.read(buff)) > 0) {
                                count += size;
                                buff.flip();
                            }
                            byte[] delta = new byte[count];
                            for (int i = 0; i < count; i++) {
                                delta[i] = buff.get(i);
                            }
                            String content = differ.vcdiffDecode(dict, delta);
                            DocArea.setText(content);
                            System.out.println("Recieved: " + content);
                            sk.interestOps(SelectionKey.OP_READ);;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Editor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Editor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Editor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Editor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new Editor().setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(Editor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea DocArea;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton syncButton;
    // End of variables declaration//GEN-END:variables
}
