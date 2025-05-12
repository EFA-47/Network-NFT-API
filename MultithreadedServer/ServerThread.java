

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import org.json.JSONArray;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;
import java.net.SocketTimeoutException;


class ServerThread extends Thread
{
    protected BufferedReader is;
    protected PrintWriter os;
    protected Socket s;
    private String line = new String();
    private String lines = new String();

    private static final int TIMEOUT_MILLISECONDS = 15000; // 15 seconds timeout

    /**
     * Creates a server thread on the input socket
     *
     * @param s input socket to create a thread on
     */
    public ServerThread(Socket s)
    {
        this.s = s;
    }

    /**
     * The server thread, echos the client until it receives the QUIT string from the client
     */
    public void run()
    {
        try
        {
            is = new BufferedReader(new InputStreamReader(s.getInputStream()));
            os = new PrintWriter(s.getOutputStream());

            
            s.setSoTimeout(TIMEOUT_MILLISECONDS);
            
            
            line = is.readLine();
            String info = "";
            while (line.compareTo("QUIT") != 0)
            {
            	lines = "Client messaged : " + line + " at  : " + Thread.currentThread().getId();
            	if(line.length()<1) {
            		info = listNFTs();
            	}else {
            		info = getInfo(line);
            	}
				
		                //os.println(lines);
				
                
                os.println(info);
                os.flush();
                System.out.println("Client " + s.getRemoteSocketAddress() + " sent :  " + lines);
                line = is.readLine();
            }
        }
        catch (SocketTimeoutException e) {
            System.err.println("Socket Timeout. Client " + s.getRemoteSocketAddress() + " timed out.");
        }
        catch (IOException e)
        {
            line = this.getName(); //reused String line for getting thread name
            System.err.println("Server Thread. Run. IO Error/ Client " + line + " terminated abruptly");
        }
        catch (NullPointerException e)
        {
            line = this.getName(); //reused String line for getting thread name
            System.err.println("Server Thread. Run.Client " + line + " Closed");
        } finally
        {
            try
            {
                System.out.println("Closing the connection");
                if (is != null)
                {
                    is.close();
                    System.err.println(" Socket Input Stream Closed");
                }

                if (os != null)
                {
                    os.close();
                    System.err.println("Socket Out Closed");
                }
                if (s != null)
                {
                    s.close();
                    System.err.println("Socket Closed");
                }

            }
            catch (IOException ie)
            {
                System.err.println("Socket Close Error");
            }
        }//end finally
    }
    public String listNFTs() throws MalformedURLException, IOException{
    	String info = "";
    	
    	String link = "https://api.coingecko.com/api/v3/nfts/list";
    	URL url = new URL(link);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        String inline = "";
        Scanner scanner = new Scanner(url.openStream());
        while (scanner.hasNext()) {
            inline += scanner.nextLine();
        }
        //Close the scanner
        scanner.close();  
    	JSONArray jsonObject = new JSONArray(inline);
        for (int i = 0; i < jsonObject.length(); i++)
        {
            String id = jsonObject.getJSONObject(i).getString("id");
            String name = jsonObject.getJSONObject(i).getString("name");
            info += i + "- " + "id: " + id + "\t" + "name: " + name + "\t";
            //System.out.println(info);
        }
    	return info;
    }
    public String getInfo(String line) throws MalformedURLException, IOException{
    	
    	String link = "https://api.coingecko.com/api/v3/nfts/" + line;
    	URL url = new URL(link);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        String inline = "";
        String info = "";
        Scanner scanner = new Scanner(url.openStream());
        while (scanner.hasNext()) {
            inline += scanner.nextLine();
        }
        //Close the scanner
        scanner.close();  
        //System.out.println(inline);
        
    	System.out.println(inline);
    	String makeJson = "[" + inline + "]";
    	JSONArray jsonObject = new JSONArray(makeJson);
    	String name = jsonObject.getJSONObject(0).getString("name");
    	String asset_platform_id = jsonObject.getJSONObject(0).getString("asset_platform_id");
    	String description = jsonObject.getJSONObject(0).getString("name");
    	
    	
    	
    	JSONArray jsonArray = new JSONArray(makeJson);
        JSONObject myjsonObject = jsonArray.getJSONObject(0);
        double floorPrice7dPercentageChange = myjsonObject.getJSONObject("floor_price_7d_percentage_change").getDouble("usd");
        
        info = name + "\t" + asset_platform_id + "\t" + floorPrice7dPercentageChange;
        
        
        
        return info;
    }
}
