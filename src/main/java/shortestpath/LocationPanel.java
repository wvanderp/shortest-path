package shortestpath;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.FlatTextField;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LocationPanel extends PluginPanel {
    private final Client client;
    private final FlatTextField locationField;
    private final JButton updateButton;

    @Inject
    public LocationPanel(Client client) {
        super();
        this.client = client;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Current Location");
        titleLabel.setFont(titleLabel.getFont().deriveFont(16f));
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout(0, 10));
        
        locationField = new FlatTextField();
        locationField.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        locationField.setPreferredSize(new Dimension(PANEL_WIDTH - 20, 30));
        locationField.setMinimumSize(new Dimension(0, 30));
        centerPanel.add(locationField, BorderLayout.CENTER);

        updateButton = new JButton("Update Location");
        updateButton.setFocusable(false);
        updateButton.setPreferredSize(new Dimension(PANEL_WIDTH - 20, 30));
        updateButton.addActionListener(e -> updateLocation());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        buttonPanel.add(updateButton, BorderLayout.NORTH);
        
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // Initial update
        updateLocation();
    }

    private void updateLocation() {
        Player localPlayer = client.getLocalPlayer();
        if (localPlayer == null) {
            locationField.setText("Not logged in");
            return;
        }

        WorldPoint location = localPlayer.getWorldLocation();
        if (location == null) {
            locationField.setText("Location unavailable");
            return;
        }

        String locationText = String.format("%d %d %d", location.getX(), location.getY(), location.getPlane());
        locationField.setText(locationText);
    }
}
