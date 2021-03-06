/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cyprusgui;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import support.Vehicle;

/**
 *
 * @author James
 */
public class ImageViewerDialog extends javax.swing.JDialog {

    /**
     * Creates new form ImageViewerDialog
     */
    public ImageViewerDialog(java.awt.Frame parent, boolean modal, Vehicle vehicle) {
        super(parent, modal);
        initComponents();
        
        if(vehicle.getImageBytes() != null && vehicle.getImageBytes().length > 0){
            try {
                Image img = ImageIO.read(new ByteArrayInputStream(vehicle.getImageBytes()));
                
                //Resize to fit this window
                Image resizedImage = img.getScaledInstance(this.getWidth() , this.getHeight(), Image.SCALE_FAST);
                imageLabel.setIcon(new ImageIcon(resizedImage));
                
                //Hide no image text  
                noImageLabel.setText(null);
            } catch (IOException ex) {
                Logger.getLogger(ImageViewerDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        noImageLabel = new javax.swing.JLabel();
        imageLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("View Image");
        setResizable(false);

        noImageLabel.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        noImageLabel.setText("No Image Found...");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(169, 169, 169)
                .addComponent(noImageLabel)
                .addContainerGap(176, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(imageLabel)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(imageLabel)
                .addGap(176, 176, 176)
                .addComponent(noImageLabel)
                .addContainerGap(225, Short.MAX_VALUE))
        );

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-560)/2, (screenSize.height-468)/2, 560, 468);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel imageLabel;
    private javax.swing.JLabel noImageLabel;
    // End of variables declaration//GEN-END:variables
}
