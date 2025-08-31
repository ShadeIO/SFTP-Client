package sftp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Scanner;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

public class SftpClient {
    public static void main(String[] args) throws SftpException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("SFTP Client");
        System.out.print("Enter the host: ");
        String host = scanner.nextLine();

        System.out.print("Enter the port: ");
        int port = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter your username: ");
        String username = scanner.nextLine();

        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        SftpManager sftpManager = new SftpManager();
        boolean connected = sftpManager.connect(host, port, username, password);

        if (!connected) {
            System.out.println("Unable to connect");
            return;
        }

        ChannelSftp channel = sftpManager.getChannel();
        String remoteFile = "domains.json";
        DomainManager domainManager = new DomainManager();
        try {
            InputStream is = channel.get(remoteFile);
            domainManager.loadFromInputStream(is);
        } catch (Exception e) {
            System.out.println("File reading error: " + e.getMessage());
        }

        System.out.println("Username: " + username);
        while (true) {
            System.out.println("\nMenu:");
            System.out.println("1. Get a list of domains");
            System.out.println("2. Find IP by domain");
            System.out.println("3. Find a domain by IP");
            System.out.println("4. Add a new pair");
            System.out.println("5. Delete a pair");
            System.out.println("6. End the program");
            System.out.print("Your choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    domainManager.printAll();
                    break;

                case "2":
                    System.out.print("Enter the domain: ");
                    String domain = scanner.nextLine();
                    String ip = domainManager.getIpByDomain(domain);
                    if (ip != null) {
                        System.out.println("IP address: " + ip);
                    } else {
                        System.out.println("Domain not found.");
                    }
                    break;

                case "3":
                    System.out.print("Enter the IP address: ");
                    String inputIp = scanner.nextLine();
                    String foundDomain = domainManager.getDomainByIp(inputIp);
                    if (foundDomain != null) {
                        System.out.println("Domain: " + foundDomain);
                    } else {
                        System.out.println("IP not found.");
                    }
                    break;

                case "4":
                    System.out.print("Enter a new domain: ");
                    String newDomain = scanner.nextLine();
                    System.out.print("Enter a new IP address: ");
                    String newIp = scanner.nextLine();

                    if (domainManager.addPair(newDomain, newIp)) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        domainManager.saveToStream(baos);
                        InputStream updatedJson = new ByteArrayInputStream(baos.toByteArray());
                        sftpManager.uploadFile(updatedJson, "domains.json");
                        System.out.println("The pair was added successfully.");
                    } else {
                        System.out.println("The domain or IP address already exists, or the IP address is incorrect.");
                    }
                    break;

                case "5":
                    System.out.print("Enter the domain or IP address to delete: ");
                    String inputToRemove = scanner.nextLine();

                    if (domainManager.removePair(inputToRemove)) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        domainManager.saveToStream(baos);
                        InputStream updatedJson = new ByteArrayInputStream(baos.toByteArray());
                        sftpManager.uploadFile(updatedJson, "domains.json");
                        System.out.println("The pair has been deleted.");
                    } else {
                        System.out.println("The pair to delete was not found.");
                    }
                    break;

                case "6":
                    System.out.println("Exiting...");
                    sftpManager.disconnect();
                    return;

                default:
                    System.out.println("Wrong choice. Try again.");
            }
        }
    }
}