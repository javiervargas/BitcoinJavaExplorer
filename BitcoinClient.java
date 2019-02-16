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
