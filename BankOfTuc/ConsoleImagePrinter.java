package BankOfTuc;

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
            BufferedImage image = loadImage(uriString);

            // Create JFrame
            JFrame frame = new JFrame(title);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            // Add JLabel with the image
            JLabel label = new JLabel(new ImageIcon(image));
            frame.getContentPane().add(label, BorderLayout.CENTER);

            frame.pack();
            frame.setLocationRelativeTo(null); // center on screen
            frame.setVisible(true);

        } catch (Exception e) {
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
