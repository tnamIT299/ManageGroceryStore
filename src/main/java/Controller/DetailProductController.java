package Controller;

import DAO.ProductDAO;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DetailProductController implements Initializable {
    @FXML
    private AnchorPane pane;
    @FXML
    private Label lb_detailProductCode;
    @FXML
    private Label lb_detailProductName;
    @FXML
    private Label lb_detailProductCategory;
    @FXML
    private Label lb_detailProductSupplier;
    @FXML
    private Label lb_detailProductCostPrice;
    @FXML
    private Label lb_detailProductSellingPrice;
    @FXML
    private Label lb_detailProductDescription;
    @FXML
    private ImageView iv_productThumbnail;
    @FXML
    private Button btn_back;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btn_back.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("views/products.fxml"));
                Node node = null;
                try {
                    node = loader.load();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                pane.getChildren().add(node);
            }
        });
        ProductDAO product=new ProductDAO();
        System.out.println("CUR productID: "+ProductController.getCurrentProductID());
        ResultSet rs = product.selectByID(ProductController.getCurrentProductID());
        try {
            if(rs.next()){
                String img  = rs.getString("THUMBNAIL");
                if(img!=null) {
                    Image image1 = new Image(String.valueOf(img));
                    iv_productThumbnail.setImage(image1);
                }
                lb_detailProductCode.setText(rs.getString("PRODUCTCODE"));
                lb_detailProductName.setText(rs.getString("PRODUCTNAME"));
                lb_detailProductCategory.setText(rs.getString("CATEGORY"));
                lb_detailProductSupplier.setText("CUC");
                lb_detailProductCostPrice.setText(String.valueOf(rs.getDouble("COSTPRICE")));
                lb_detailProductSellingPrice.setText(String.valueOf(rs.getDouble("SELLINGPRICE")));
                lb_detailProductDescription.setText("CHUA CO");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }
}
