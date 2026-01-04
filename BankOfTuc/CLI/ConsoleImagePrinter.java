package BankOfTuc.CLI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Base64;

public class ConsoleImagePrinter {

    //display image from a URI string (data:, file:, http:) in a JFrame
    public static void showQrImage(String uriString, String title) {
        try {
            final BufferedImage image = loadImage(uriString);

            // show image on the Swing EDT and scale if too big for the screen
            SwingUtilities.invokeLater(() -> {
                try {
                    int screenW = Toolkit.getDefaultToolkit().getScreenSize().width;
                    int screenH = Toolkit.getDefaultToolkit().getScreenSize().height;
                    int maxW = (int) (screenW * 0.8);
                    int maxH = (int) (screenH * 0.8);

                    BufferedImage displayImg = image;
                    if (image.getWidth() > maxW || image.getHeight() > maxH) {
                        double sx = (double) maxW / image.getWidth();
                        double sy = (double) maxH / image.getHeight();
                        double s = Math.min(sx, sy);
                        int w = Math.max(1, (int) (image.getWidth() * s));
                        int h = Math.max(1, (int) (image.getHeight() * s));
                        Image tmp = image.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                        BufferedImage resized = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g2 = resized.createGraphics();
                        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        g2.drawImage(tmp, 0, 0, null);
                        g2.dispose();
                        displayImg = resized;
                    }

                    JFrame frame = new JFrame(title);
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                    JLabel label = new JLabel(new ImageIcon(displayImg));
                    label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    frame.getContentPane().add(label, BorderLayout.CENTER);

                    frame.pack();
                    frame.setLocationRelativeTo(null); // center on screen
                    frame.setVisible(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

        } catch (Exception e) {
            // fallback: print data URI so user can open it elsewhere
            System.err.println("Could not display QR image, please open this URI manually: " + uriString);
            e.printStackTrace();
        }
    }

    // load BufferedImage from data URI, file, or HTTP/HTTPS
    private static BufferedImage loadImage(String uriString) throws Exception {
        if (uriString.startsWith("data:")) {
            // data URI
            String base64Data = uriString.substring(uriString.indexOf(",") + 1);
            byte[] bytes = Base64.getDecoder().decode(base64Data);
            try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
                return ImageIO.read(in);
            }
        }

        URI uri = new URI(uriString);
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            return ImageIO.read(new java.io.File(uri));
        } else {
            try (InputStream in = uri.toURL().openStream()) {
                return ImageIO.read(in);
            }
        }
    }
}
