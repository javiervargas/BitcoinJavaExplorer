import javax.xml.bind.DatatypeConverter;
import java.util.Hashtable;
import java.util.Enumeration;
import java.math.BigDecimal;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.*;
import wf.bitcoin.krotjson.HexCoder;
import java.text.DecimalFormat;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Transaction;
import java.util.ListIterator;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.List;
import java.io.*;
import java.util.Properties;
import java.util.Objects;

public class BitcoinClient{

        BitcoinJSONRPCClient bitcoinClient = null;
        DAO dao = new DAO();
        BigDecimal min = new BigDecimal("0");
        BigDecimal max = new BigDecimal("0");
        String address = "";
        String maxs;
        String mins;
        static class Tx {
                String txid;
                String address;
                String value;
                String hex;

                public String getHex(){
                        return this.hex;
                }

                public void setHex(String hex){
                        this.hex = hex;
                }

                public String getTxid(){
                        return this.txid;
                }
                public void setTxid(String txid){
                        this.txid = txid;
                }

                public String getAddress(){
                        return this.address;
                }
                   public String getValue(){
                        return this.value;
                }
                public void setValue(String value){
                        this.value = value;
                }
        }

        public Integer getBlockCount(){
                return bitcoinClient.getBlockCount();
        }

        public boolean connect(String user,String pwd,String host,String port){
                try {
                        URL url = new URL("http://" + user + ':' + pwd + "@" + host + ":" + port + "/");
                        bitcoinClient = new BitcoinJSONRPCClient(url);
                        return true;
                }catch (MalformedURLException e) {
                        e.printStackTrace();
                        return false;
                }
        }

        public Integer getSize(){
                return bitcoinClient.getBlock(bitcoinClient.getBlockCount()).size();
        }
        public String getHash(){
                return bitcoinClient.getBlock(bitcoinClient.getBlockCount()).hash();
        }
        public Integer getConfirmations(){
                return bitcoinClient.getBlock(bitcoinClient.getBlockCount()).confirmations();
        }
        public Integer getHeight(){
                return bitcoinClient.getBlock(bitcoinClient.getBlockCount()).height();
        }
        public Hashtable listTx(int blocknum, BigDecimal value){
                 OutputStream opStream = null;
                 Hashtable<String,Tx> txmap = new Hashtable<String,Tx>();

                 try{
                         File f = new File("/var/www/blockchain.txt");
                         if (!f.exists()) {
                                f.createNewFile();
                         }
                         opStream = new FileOutputStream(f);
                         int txcounter =0;
                         int j = 0;
                        for (int i=blocknum;i<=blocknum;i++){
                                List tx = bitcoinClient.getBlock(i).tx();
                                txmap = new Hashtable<String,Tx>();
                                if(blocknum>0){
                                        for(j=0;j<tx.size();j++){
                                        String rawtxt = bitcoinClient.getRawTransactionHex(tx.get(j).toString());
                                        BitcoindRpcClient.RawTransaction  txt = bitcoinClient.decodeRawTransaction(rawtxt);
                                                ListIterator<BitcoindRpcClient.RawTransaction.Out> voutIterator = txt.vOut().listIterator();
                                                 while (voutIterator.hasNext()) {
                                                        BitcoindRpcClient.RawTransaction.Out next = voutIterator.next();
                                                        BigDecimal txValue = next.value();
                                                        if(txValue.compareTo(max)==1){
                                                                max = txValue;
                                                        }
                                                        min = max;
                                                        if(txValue.compareTo(min)==-1){
                                                                min = txValue;
                                                        }
                                                        try{
                                                                ListIterator<String> addressesIterator = next.scriptPubKey().addresses().listIterator();
                                                                while (addressesIterator.hasNext()) {
                                                                    String outAddress = addressesIterator.next();
                                                                    DecimalFormat df = new DecimalFormat("#.########");
                                                                    maxs = df.format(max);
                                                                    mins = df.format(min);
                                                                    Tx t = new Tx();
                                                                    t.setTxid(txt.txId());
                                                                    t.setAddress(outAddress);
                                                                    t.setValue(df.format(txValue));
                                                                    String s_hex = next.scriptPubKey().asm();
                                                                    t.setHex(s_hex);
                                                                    txmap.put(txt.txId(),t);
                                                                 if(txValue.compareTo(value)==1){
                                                                    System.out.println(txt.txId() + " " +df.format(txValue)+ " " + outAddress + " " +t.getHex());
                                                                    String strContent = bitcoinClient.getBlock(i).height()+";"+txt.txId()+";"+outAddress+";"+df.format(txValue)+";"+t.getHex();
                                                                    byte[] byteContent = strContent.getBytes();
                                                                    try{
                                                                            opStream.write(byteContent);
                                                                            String lineSeparator = System.getProperty("line.separator");
                                                                            opStream.write(lineSeparator.getBytes());
                                                                            opStream.flush();
                                                                    }catch(IOException ee){}
                                                                 }
                                                                }
                                                        }catch(java.lang.NullPointerException e){}
                                                }
                                        }
                                }
                                max = min;
                   }
                }
          catch (IOException e) {
                    e.printStackTrace();
                }
                finally{
                    try{
                       if(opStream != null) opStream.close();
                    } catch(Exception ex){}
                }
                return txmap;
        }

        public static void main(String args[]){
                int txcounter =0;
                BigDecimal min = new BigDecimal(args[1]);
                Hashtable<String,Tx> txmap = new Hashtable<String,Tx>();
                BitcoinClient bitcoin = new BitcoinClient();
                Properties prop = new Properties();
                try {
                        String fileName = "config.properties";
                        ClassLoader classLoader = BitcoinClient.class.getClassLoader();
                        URL res = Objects.requireNonNull(classLoader.getResource(fileName),"");
                        InputStream is = new FileInputStream(res.getFile());
                        prop.load(is);
                } catch (IOException e) {
                        e.printStackTrace();
                }
                bitcoin.connect(prop.getProperty("user"),prop.getProperty("pwd"),prop.getProperty("host"),prop.getProperty("port"));
                txmap = bitcoin.listTx(Integer.parseInt(args[0]),min);
                System.out.println("Total transacciones: " +txmap.size());
                BitcoinClient.Tx tmax = new BitcoinClient.Tx();
        }

}
              
