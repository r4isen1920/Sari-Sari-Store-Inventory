package sari_sari_manager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * A simple class for managing the inventory items
 * @author Raisen Cadavez
 */
public class InventoryManager {
    /**
     * Instance of inventory item
     */
    private Map<String, Item> inventory;

    public static void main(String[] args) {
        InventoryManager inventoryManager = new InventoryManager();
        Scanner scanner = new Scanner(System.in);

        JFrame frame = new JFrame("Inventory Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);
        frame.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new FlowLayout());
        JTextField itemNameField = new JTextField(15);
        JTextField stockCountField = new JTextField(5);
        JTextField pricePerItemField = new JTextField(8);
        JTextField barcodeIdField = new JTextField(10);
        JButton addItemButton = new JButton("Add/Update Item");
        JButton saveInventoryButton = new JButton("Save Inventory");
        JButton editItemButton = new JButton("Edit Selected Item");
        JButton removeItemButton = new JButton("Remove Selected Item");

        String[] columnNames = {"Barcode ID", "Item Name", "Stock Count", "Price per Item"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable inventoryTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(inventoryTable);

        for (Map.Entry<String, Item> entry : inventoryManager.inventory.entrySet()) {
            Item item = entry.getValue();
            tableModel.addRow(new Object[]{item.barcodeId, item.itemName, item.stockCount, item.pricePerItem});
        }

        addItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String itemName = itemNameField.getText();
                String stockCountText = stockCountField.getText();
                String pricePerItemText = pricePerItemField.getText();
                String barcodeId = barcodeIdField.getText();

                if (!itemName.isEmpty() && !stockCountText.isEmpty() && !pricePerItemText.isEmpty() && !barcodeId.isEmpty()) {
                    try {
                        int stockCount = Integer.parseInt(stockCountText);
                        double pricePerItem = Double.parseDouble(pricePerItemText);

                        inventoryManager.addItem(itemName, stockCount, pricePerItem, barcodeId);
                        inventoryManager.saveInventoryToFile();

                        // Update table
                        boolean itemExists = false;
                        for (int i = 0; i < tableModel.getRowCount(); i++) {
                            if (tableModel.getValueAt(i, 0).equals(barcodeId)) {
                                tableModel.setValueAt(itemName, i, 1);
                                tableModel.setValueAt(stockCount, i, 2);
                                tableModel.setValueAt(pricePerItem, i, 3);
                                itemExists = true;
                                break;
                            }
                        }
                        if (!itemExists) {
                            tableModel.addRow(new Object[]{barcodeId, itemName, stockCount, pricePerItem});
                        }

                        itemNameField.setText("");
                        stockCountField.setText("");
                        pricePerItemField.setText("");
                        barcodeIdField.setText("");
                        JOptionPane.showMessageDialog(frame, "Item added/updated successfully.");
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame, "Invalid number format for stock count or price.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        saveInventoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inventoryManager.saveInventoryToFile();
                JOptionPane.showMessageDialog(frame, "Inventory records saved to: " + new java.io.File("inventory.json").getAbsolutePath());
            }
        });

        editItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = inventoryTable.getSelectedRow();
                if (selectedRow != -1) {
                    String barcodeId = (String) tableModel.getValueAt(selectedRow, 0);
                    String itemName = (String) tableModel.getValueAt(selectedRow, 1);
                    int stockCount = (Integer) tableModel.getValueAt(selectedRow, 2);
                    double pricePerItem = (Double) tableModel.getValueAt(selectedRow, 3);

                    String newItemName = JOptionPane.showInputDialog(frame, "Edit Item Name", itemName);
                    if (newItemName != null && !newItemName.trim().isEmpty()) {
                        try {
                            int newStockCount = Integer.parseInt(JOptionPane.showInputDialog(frame, "Edit Stock Count", stockCount));
                            double newPricePerItem = Double.parseDouble(JOptionPane.showInputDialog(frame, "Edit Price per Item", pricePerItem));

                            inventoryManager.editItem(barcodeId, newItemName, newStockCount, newPricePerItem);
                            inventoryManager.saveInventoryToFile();

                            tableModel.setValueAt(newItemName, selectedRow, 1);
                            tableModel.setValueAt(newStockCount, selectedRow, 2);
                            tableModel.setValueAt(newPricePerItem, selectedRow, 3);
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(frame, "Invalid number format for stock count or price.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Please select an item to edit.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        removeItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = inventoryTable.getSelectedRow();
                if (selectedRow != -1) {
                    String barcodeId = (String) tableModel.getValueAt(selectedRow, 0);
                    inventoryManager.removeItem(barcodeId);
                    inventoryManager.saveInventoryToFile();
                    tableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(frame, "Item removed from inventory.");
                } else {
                    JOptionPane.showMessageDialog(frame, "Please select an item to remove.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        inputPanel.add(new JLabel("Item Name:"));
        inputPanel.add(itemNameField);
        inputPanel.add(new JLabel("Stock Count:"));
        inputPanel.add(stockCountField);
        inputPanel.add(new JLabel("Price per Item:"));
        inputPanel.add(pricePerItemField);
        inputPanel.add(new JLabel("Barcode ID:"));
        inputPanel.add(barcodeIdField);
        inputPanel.add(addItemButton);
        inputPanel.add(editItemButton);
        inputPanel.add(removeItemButton);
        inputPanel.add(saveInventoryButton);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(tableScrollPane, BorderLayout.CENTER);

        frame.setVisible(true);

        /**
         * Usage of CLI for the demonstration with the scanner.util
         */
        while (true) {
            System.out.println("Enter command (add, edit, remove, save, exit):");
            String command = scanner.nextLine();

            if (command.equalsIgnoreCase("exit")) {
                break;
            } else if (command.equalsIgnoreCase("add")) {
                System.out.println("Enter item name:");
                String itemName = scanner.nextLine();
                System.out.println("Enter stock count:");
                int stockCount = Integer.parseInt(scanner.nextLine());
                System.out.println("Enter price per item:");
                double pricePerItem = Double.parseDouble(scanner.nextLine());
                System.out.println("Enter barcode ID:");
                String barcodeId = scanner.nextLine();

                inventoryManager.addItem(itemName, stockCount, pricePerItem, barcodeId);
                inventoryManager.saveInventoryToFile();
                System.out.println("Item added/updated successfully.");
            } else if (command.equalsIgnoreCase("edit")) {
                System.out.println("Enter barcode ID of item to edit:");
                String barcodeId = scanner.nextLine();
                
                if (inventoryManager.inventory.containsKey(barcodeId)) {
                    System.out.println("Enter new item name:");
                    String itemName = scanner.nextLine();
                    System.out.println("Enter new stock count:");
                    int stockCount = Integer.parseInt(scanner.nextLine());
                    System.out.println("Enter new price per item:");
                    double pricePerItem = Double.parseDouble(scanner.nextLine());

                    inventoryManager.editItem(barcodeId, itemName, stockCount, pricePerItem);
                    inventoryManager.saveInventoryToFile();
                    System.out.println("Item edited successfully.");
                } else {
                    System.out.println("Item not found.");
                }
            } else if (command.equalsIgnoreCase("remove")) {
                System.out.println("Enter barcode ID of item to remove:");
                String barcodeId = scanner.nextLine();
                if (inventoryManager.inventory.containsKey(barcodeId)) {
                    inventoryManager.removeItem(barcodeId);
                    inventoryManager.saveInventoryToFile();
                    System.out.println("Item removed successfully.");
                } else {
                    System.out.println("Item not found.");
                }
            } else if (command.equalsIgnoreCase("save")) {
                inventoryManager.saveInventoryToFile();
                System.out.println("Inventory saved successfully.");
            } else {
                System.out.println("Invalid command.");
            }
        }

        scanner.close();
    }
 
    /**
     * Returns this class
     */
    public InventoryManager() {
        inventory = new HashMap<>();
        loadInventoryFromFile();
    }

    /**
     * Adds a new item in the list
     * @param itemName the name of the item to add
     * @param stockCount the number of stocks the item has
     * @param pricePerItem the price of the item in PHP
     * @param barcodeId the unique barcode id
     */
    public void addItem(String itemName, int stockCount, double pricePerItem, String barcodeId) {
        inventory.put(barcodeId, new Item(itemName, stockCount, pricePerItem, barcodeId));
    }

    /**
     * Edits an existing item
     * @param barcodeId the modified barcode id
     * @param itemName the modified item name
     * @param stockCount the modified stock count
     * @param pricePerItem the modified price per item
     */
    public void editItem(String barcodeId, String itemName, int stockCount, double pricePerItem) {
        if (inventory.containsKey(barcodeId)) {
            inventory.put(barcodeId, new Item(itemName, stockCount, pricePerItem, barcodeId));
        }
    }
  
    /**
     * Removes an item from the inventory
     * @param barcodeId the barcode id of the item to remove
     */
    public void removeItem(String barcodeId) {
        inventory.remove(barcodeId);
    }

    /**
     * Saves the inventory data into a file
     * Since we do not have a database, this is a temporary solution
     */
    public void saveInventoryToFile() {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, Item> entry : inventory.entrySet()) {
            Item item = entry.getValue();
            JSONObject itemJson = new JSONObject();
            itemJson.put("itemName", item.itemName);
            itemJson.put("stockCount", item.stockCount);
            itemJson.put("pricePerItem", item.pricePerItem);
            json.put(entry.getKey(), itemJson);
        }

        try (FileWriter writer = new FileWriter("inventory.json")) {
            writer.write(json.toString(4));
            System.out.println("Inventory records saved to " + new java.io.File("inventory.json").getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Error while saving inventory records: " + e.getMessage());
        }
    }

    /**
     * Reads the file and loads the inventory
     */
    public void loadInventoryFromFile() {
        try (FileReader reader = new FileReader("inventory.json")) {
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject jsonObject = new JSONObject(tokener);

            for (String barcodeId : jsonObject.keySet()) {
                JSONObject itemObject = jsonObject.getJSONObject(barcodeId);
                String itemName = itemObject.getString("itemName");
                int stockCount = itemObject.getInt("stockCount");
                double pricePerItem = itemObject.getDouble("pricePerItem");
                inventory.put(barcodeId, new Item(itemName, stockCount, pricePerItem, barcodeId));
            }
        } catch (IOException e) {
            System.out.println("No previous inventory file found. Starting fresh.");
        }
    }

    // Item class to store item details
    static class Item {
        String itemName;
        int stockCount;
        double pricePerItem;
        String barcodeId;

        public Item(String itemName, int stockCount, double pricePerItem, String barcodeId) {
            this.itemName = itemName;
            this.stockCount = stockCount;
            this.pricePerItem = pricePerItem;
            this.barcodeId = barcodeId;
        }
    }
}