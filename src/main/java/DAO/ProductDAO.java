package DAO;

import Model.InventoryAlert;
import DatabaseConnection.ConnectionFactory;
import Model.CameraApp;
import Model.Product;

import java.sql.*;
import java.util.ArrayList;

public class ProductDAO extends InventoryAlert implements DaoInterface<Product> {
    Connection con = null;
    PreparedStatement pstmt = null;
    Statement stmt = null;
    ResultSet rs1=null;
    Statement stmt1=null;
    ResultSet rs = null;
    public static ProductDAO getInstance(){
        return new ProductDAO();
    }
    public ProductDAO(){
        try {
            con = new ConnectionFactory().getConnection();
            stmt = con.createStatement();
            stmt1=con.createStatement();
            Stocks stocks = new Stocks();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public int insert(Product product) {
        try{
            String query = "SELECT pid FROM products WHERE PRODUCTBARCODE=?";
            pstmt = con.prepareStatement(query);
            pstmt.setString(1,product.getProductBarCode());
            rs=pstmt.executeQuery();
            if(rs.next()){
                errorAlert("ERROR","THIS PRODUCT HAS BEEN ADDED!");
                return 0;
            }else{
                addFunction(product);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return 1;
    }

    @Override
    public int delete(int pid) {
        String deleteProduct= "DELETE FROM products\n" +
                "WHERE pid = ?\n" +
                "AND PID NOT IN (SELECT DISTINCT productID FROM detailbill);";
        int result;
        try {
            pstmt = con.prepareStatement(deleteProduct);
            pstmt.setInt(1,pid);
            result=pstmt.executeUpdate();
            if(result==0)
                errorAlert("Error","YOU CANNOT REMOVE THIS PRODUCT BECAUSE THE INVOICE DETAILS IS REFERRED TO");
            else
                informationAlert("Success","DELETE SUCCESSFUL");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public int update(Product product) {
        int affectedRow=0;
        String updateProduct = "UPDATE products SET categoryid = ? thumbnail =? WHERE pid=?";
        try {
            pstmt = con.prepareStatement(updateProduct);
            pstmt.setInt(1,product.getCategoryID());
            pstmt.setString(2, product.getThumbnailLink());
            pstmt.setInt(3,product.getProductId());
            affectedRow+=pstmt.executeUpdate();
            if(affectedRow>0)
                addProductPrices(product);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return affectedRow;
    }

    @Override
    public ResultSet selectALL(int Limit, int offSet) {
        try {
            String selectAllProduct = "SELECT * FROM productList ORDER BY pid ASC ";
            pstmt= con.prepareStatement(selectAllProduct);
            rs = pstmt.executeQuery();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        return  rs;
    }

    public ArrayList<Product> selectALL1() {
        ArrayList<Product> productList = new ArrayList<>();
        try {
            String selectAllProduct = "SELECT * FROM products ORDER BY productName ASC ";
            pstmt= con.prepareStatement(selectAllProduct);
            rs = pstmt.executeQuery();
            while (rs.next())
                productList.add(new Product(rs.getString("PRODUCTNAME"), rs.getString("PRODUCTBARCODE"), rs.getInt("PID"),rs.getString("THUMBNAIL")));
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        return  productList;
    }
    @Override
    public ResultSet selectByID(int ID) {
        ResultSet rs;
        try {

            String selectByID_query = "SELECT THUMBNAIL, PRODUCTNAME, Categoryid,P.Pid, COSTPRICE, SELLINGPRICE, PRODUCTBARCODE, manufractureDate, expirationDate,SID  FROM productList WHERE P.Pid =? ";
            pstmt= con.prepareStatement(selectByID_query);
            pstmt.setInt(1,ID);
            rs =pstmt.executeQuery();
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return rs;
    }

    @Override
    public int addFunction(Product product) {
        int result=0;
        try {
            String productSKUCode = null;
            productSKUCode = product.getProductBarCode()+" "+product.getMFGDate();
            String addProduct= "INSERT INTO PRODUCTS (productBarCode, productname,sid,categoryid,productSKU,thumbnail)"
                    +"VALUE(?,?,?,?,?,?,?,?)";
            pstmt = con.prepareStatement(addProduct);
            pstmt.setString(1,product.getProductBarCode());
            pstmt.setString(2, product.getProductName());
            pstmt.setInt(3,product.getSupplierID());
            pstmt.setInt(4,product.getCategoryID());
            pstmt.setString(5,productSKUCode);
            pstmt.setString(6,product.getThumbnailLink());
            pstmt.executeUpdate();
            String skuFileName=product.getProductName()+product.getMFGDate();
            CameraApp.skuGenerate(productSKUCode,skuFileName+".png");
            String getPid ="SELECT LAST_INSERT_ID()";
            rs=pstmt.executeQuery(getPid);
            int pid=0;
            if(rs.next()) {
                pid = rs.getInt("LAST_INSERT_ID()");
            }
            product.setProductId(pid);
            addProductPrices(product);
            addProductBatch(product);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    public long getQuantity(int pid){
        String query="SELECT SUM(quantity) AS totalQuantity FROM PRODUCTBATCH WHERE pid = ?";
        long totalQuantity=0;
        try {
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1,pid);
            rs =pstmt.executeQuery();
            if(rs.next())
                totalQuantity=rs.getInt("totalQuantity");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return totalQuantity;
    }
    public void addProductBatch(Product product){

        try {
            String addProductBatch= "INSERT INTO PRODUCTBATCH (pid, expirationDate, manufractureDate, quantity)"
                    +"VALUE(?,?,?,?)";
            pstmt = con.prepareStatement(addProductBatch);
            pstmt.setInt(1,product.getProductId());
            pstmt.setDate(2, Date.valueOf(product.getEXPDate()));
            pstmt.setDate(3,Date.valueOf(product.getMFGDate()));
            pstmt.setInt(4,product.getQuantity());
            int affectedRows = pstmt.executeUpdate();
            if(affectedRows>0)
                errorAlert("Success","ADDED NEW BATCH SUCCESSFUL");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    public int getNumProuduct(){
        String query="SELECT COUNT(pid) as numberProduct FROM products";
        int numberProduct=0;
        try {
            pstmt = con.prepareStatement(query);
            rs = pstmt.executeQuery();
            if(rs.next())
                numberProduct=rs.getInt("numberProduct");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return numberProduct;
    }
    public ResultSet getTopProducts(){
        try {
            CallableStatement cstmt = con.prepareCall("{ CALL getTopProducts() }");
            rs = cstmt.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return rs;
    }
    public ResultSet getAllProductBatchByID(int ID){
        String query = "SELECT manufractureDate, quantity FROM  productbatch WHERE QUANTITY>0 AND PID=?";
        try {
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1,ID);
            rs=pstmt.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return rs;
    }
    public void updateOldBatch(Product product){
        String updateProductBatch = "UPDATE PRODUCTBATCH SET Quantity=?,importDate=CURRENT_TIMESTAMP WHERE manufractureDate=? AND PID =?";
        try {
            pstmt = con.prepareStatement(updateProductBatch);
            pstmt.setInt(1, product.getQuantity());
            pstmt.setDate(2,Date.valueOf(product.getMFGDate()));
            pstmt.setInt(3,product.getProductId());
            int affectedRow = pstmt.executeUpdate();
            if(affectedRow>0)
                informationAlert("Update","THE BATCH OF PRODUCT "+product.getProductId()+" PRODUCED ON "+product.getMFGDate()+" HAS BEEN UPDATED WITH THE QUANTITY");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public ResultSet getAllDateByID(int ID){
        String query = "SELECT manufractureDate, expirationDate FROM  productbatch WHERE QUANTITY>0 AND PID=?";
        try {
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1,ID);
            rs=pstmt.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return rs;
    }
    private void addProductPrices(Product product){
        String query = "SELECT ID FROM productprices WHERE PID =? AND costPrice = ?";
        try {
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1,product.getProductId());
            pstmt.setDouble(2,product.getCostPrice());
            if(rs.next()){
            }else {
                String query2= "INSERT INTO productPrices (costPrice, discountRate, pid)\n" +
                        "VALUES (?,?,?)";
                pstmt = con.prepareStatement(query2);
                pstmt.setDouble(1,product.getCostPrice());
                pstmt.setFloat(2,0);
                pstmt.setInt(3, product.getProductId());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public ArrayList<Product> searchName(String searchValue){
        ArrayList<Product> productSearchList= new ArrayList<>();
        String fullTextSearches="SELECT *\n" +
                "FROM productlist\n" +
                "WHERE MATCH(productname) AGAINST (?);\n";
        try {
            pstmt = con.prepareStatement(fullTextSearches);
            pstmt.setString(1,searchValue);
            rs = pstmt.executeQuery();
            while (rs.next())
                productSearchList.add(new Product(rs.getString("PRODUCTNAME"), rs.getString("PRODUCTBARCODE"), rs.getInt("PID"),rs.getDouble("COSTPRICE"),rs.getString("THUMBNAIL")));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return productSearchList;
    }
}
