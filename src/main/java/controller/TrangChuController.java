/*
 * @ (#) TrangChuController.java       1.0     28/09/2024
 *
 *Copyright (c) 2024 IUH. All right reserved.
 */
package controller;

import com.jfoenix.controls.JFXButton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcons;
import dto.VeDTO;
import entity.*;
import gui.TrangChu_GUI;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.SneakyThrows;
import service.*;
import util.VeMapper;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/*
 * @description:
 * @author: Hiep Nguyen
 * @date:   28/09/2024
 * version: 1.0
 */
public class TrangChuController implements Initializable {

    @FXML
    private AnchorPane paneMain;
    @FXML
    private AnchorPane acpVe;
    @FXML
    private AnchorPane acpTK;
    @FXML
    private AnchorPane acpQLTK;
    @FXML
    private AnchorPane acpNV;
    @FXML
    private AnchorPane acpQL;
    @FXML
    private JFXButton btnFVe;
    @FXML
    private JFXButton btnFQLHD;
    @FXML
    private JFXButton btnQLNV;
    @FXML
    private JFXButton btnFKH;
    @FXML
    private JFXButton btnFBCTK;
    @FXML
    private JFXButton btnQLBCTK;
    @FXML
    private JFXButton btnTKDoanhThu;
    @FXML
    private JFXButton btnTKNV;
    @FXML
    private JFXButton btnFCT;
    @FXML
    private JFXButton btnFKM;
    @FXML
    private JFXButton btnUM;
    @FXML
    private JFXButton btnBanVeGUI;
    @FXML
    private JFXButton btnDoiVeGUI;
    @FXML
    private JFXButton btnHuyVeGUI;

    @FXML
    private JFXButton btnQLCT;

    @FXML
    private JFXButton btnQLHD;

    @FXML
    private JFXButton btnQLKH;

    @FXML
    private JFXButton btnQLKM;

    @FXML
    private JFXButton btnUMQL;

    @FXML
    private JFXButton btnQLTKDoanhThu;

    @FXML
    private JFXButton btnQLTKNV;

    @FXML
    private Label lblTenNhanVien;
    @FXML
    private Label timer;
    @FXML
    private Label lblNgay;
    @FXML
    private FontAwesomeIcon iconDown;
    @FXML
    private FontAwesomeIcon iconDownTK;
    @FXML
    private FontAwesomeIcon iconDownQLTK;
    @FXML
    private FontAwesomeIcon iconCT;

    @FXML
    private FontAwesomeIcon iconHD;

    @FXML
    private FontAwesomeIcon iconHK;

    @FXML
    private FontAwesomeIcon iconHelp;

    @FXML
    private FontAwesomeIcon iconNV;

    @FXML
    private FontAwesomeIcon iconTK;

    @FXML
    private FontAwesomeIcon iconKM;


    private VeService veService;
    private CT_LichTrinhService ctltService;
    private CT_HoaDonService cthdService;
    private HoaDonService hdService;
    private LichTrinhService lichTrinhService;


    Time time = new Time(DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()));
    private String style = null;
    private String styleLV2 = null;
    Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(1),
                    e -> {
                        time.oneSecondPassed();
                        timer.setText(time.getCurrentTime());
                    }));

    @SneakyThrows
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initService();
        if (Objects.equals(getData.nv.getChucVu(), "Nhân viên")) {
            acpNV.setDisable(false);
            acpQL.setDisable(true);
            acpQL.setVisible(false);
        } else {
            acpNV.setDisable(true);
            acpQL.setDisable(false);
            acpQL.setVisible(true);
        }
        paneMain.getChildren().clear();
        try {
            FXMLLoader loader = new FXMLLoader(TrangChu_GUI.class.getResource("gioi-thieu.fxml"));
            double width = paneMain.getWidth();
            double height = paneMain.getHeight();
            paneMain.getChildren().clear();
            paneMain.getChildren().add(loader.load());
            paneMain.setPrefSize(width, height);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        lblNgay.setText(DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDate.now()));
        timer.setText(time.getCurrentTime());
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        style = btnFVe.getStyle();
        styleLV2 = btnBanVeGUI.getStyle();
        iconDown.setIcon(FontAwesomeIcons.ANGLE_DOWN);
        btnBanVeGUI.setOnMouseClicked(e -> chooseFeatureButtonLV2(btnBanVeGUI));
        btnDoiVeGUI.setOnMouseClicked(e -> chooseFeatureButtonLV2(btnDoiVeGUI));
        btnHuyVeGUI.setOnMouseClicked(e -> chooseFeatureButtonLV2(btnHuyVeGUI));
        btnTKDoanhThu.setOnMouseClicked(e -> chooseFeatureButtonLV2(btnTKDoanhThu));
        btnTKNV.setOnMouseClicked(e -> chooseFeatureButtonLV2(btnTKNV));
        btnFVe.setOnMouseClicked(e -> {
            try {
                lichTrinhService.updateTrangThaiCT(false);
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
            List<Button> dsbtnFVe = List.of(btnBanVeGUI, btnDoiVeGUI, btnHuyVeGUI);
            dsbtnFVe.forEach(btn -> {
                btn.setStyle(styleLV2);
            });
            resetFBCTK();
            if (iconDown.getText().equals("\uF107")) {
                btnFVe.setStyle(style + "-fx-border-width: 2 0 0 0;");
                chooseFeatureButton(btnFVe);
                iconDown.setIcon(FontAwesomeIcons.ANGLE_UP);
                acpVe.setVisible(true);
                List<Button> dsF = List.of(btnFQLHD, btnFKH, btnFBCTK, btnFCT, btnFKM,btnUM);
                dsF.forEach(btn -> {
                    btn.setLayoutY(btn.getLayoutY() + acpVe.getHeight());
                });
                iconDownTK.setLayoutY(iconDownTK.getLayoutY() + acpVe.getHeight());
            } else {
                onClick();
                btnFVe.setStyle(style + "-fx-border-width: 0 0 2 0;");
                iconDown.setIcon(FontAwesomeIcons.ANGLE_DOWN);
                acpVe.setVisible(false);
                List<Button> dsF = List.of(btnFQLHD, btnFKH, btnFBCTK, btnFCT, btnFKM,btnUM);
                dsF.forEach(btn -> {
                    btn.setLayoutY(btn.getLayoutY() - acpVe.getHeight());
                });

                iconDownTK.setLayoutY(iconDownTK.getLayoutY() - acpVe.getHeight());
            }
        });
        btnQLNV.setOnMouseClicked(e -> {
            chooseFeatureButton(btnQLNV);
            resetFVe();
            resetFBCTK();
        });
        btnFQLHD.setOnMouseClicked(e -> {
            chooseFeatureButton(btnFQLHD);
            resetFVe();
            resetFBCTK();
        });
        btnFKH.setOnMouseClicked(e -> {
            chooseFeatureButton(btnFKH);
            resetFVe();
            resetFBCTK();
        });
        btnFBCTK.setOnMouseClicked(e -> {
            resetFVe();
            if (iconDownTK.getText().equals("\uF107")) {
                chooseFeatureButton(btnFBCTK);
                iconDownTK.setIcon(FontAwesomeIcons.ANGLE_UP);
                acpTK.setVisible(true);
                btnUM.setLayoutY(btnUM.getLayoutY() + acpTK.getHeight());
                iconHelp.setLayoutY(iconHelp.getLayoutY() + acpTK.getHeight());
            } else {
                onClick();
                btnFBCTK.setStyle(style + "-fx-border-width: 0 0 2 0;");
                iconDownTK.setIcon(FontAwesomeIcons.ANGLE_DOWN);
                acpTK.setVisible(false);
                btnUM.setLayoutY(btnUM.getLayoutY() - acpTK.getHeight());
                iconHelp.setLayoutY(iconHelp.getLayoutY() - acpTK.getHeight());
            }
        });
        btnQLBCTK.setOnMouseClicked(e -> {
            if (iconDownQLTK.getText().equals("\uF107")) {
                chooseFeatureButtonQL(btnQLBCTK);
                iconDownQLTK.setIcon(FontAwesomeIcons.ANGLE_UP);
                acpQLTK.setVisible(true);
                btnUMQL.setLayoutY(btnUMQL.getLayoutY() + acpQLTK.getHeight());
                iconHelp.setLayoutY(iconHelp.getLayoutY() + acpQLTK.getHeight());
            } else {
                onClick();
                btnQLBCTK.setStyle(style + "-fx-border-width: 0 0 2 0;");
                iconDownQLTK.setIcon(FontAwesomeIcons.ANGLE_DOWN);
                acpQLTK.setVisible(false);
                btnUMQL.setLayoutY(btnUMQL.getLayoutY() - acpQLTK.getHeight());
                iconHelp.setLayoutY(iconHelp.getLayoutY() - acpQLTK.getHeight());
            }
        });
        btnQLNV.setOnMouseClicked(e -> {
            chooseFeatureButtonQL(btnQLNV);
            resetQLBCTK();
        });
        btnQLCT.setOnMouseClicked(e -> {
            chooseFeatureButtonQL(btnQLCT);
            resetQLBCTK();
        });
        btnQLKH.setOnMouseClicked(e -> {
            chooseFeatureButtonQL(btnQLKH);
            resetQLBCTK();
        });
        btnQLKM.setOnMouseClicked(e -> {
            chooseFeatureButtonQL(btnQLKM);
            resetQLBCTK();
        });

        btnQLHD.setOnMouseClicked(e -> {
            chooseFeatureButtonQL(btnQLHD);
            resetQLBCTK();
        });

        btnQLTKDoanhThu.setOnMouseClicked(e -> {
            chooseFeatureButtonQLLV2(btnQLTKDoanhThu);
        });
        btnQLTKNV.setOnMouseClicked(e -> {
            chooseFeatureButtonQLLV2(btnQLTKNV);
        });

        btnFCT.setOnMouseClicked(e -> {
            chooseFeatureButton(btnFCT);
            resetFVe();
            resetFBCTK();
        });
        btnFKM.setOnMouseClicked(e -> {
            chooseFeatureButton(btnFKM);
            resetFVe();
            resetFBCTK();
        });
        TrangChu_GUI.nv = getData.nv;
        lblTenNhanVien.setText("Chào, " + TrangChu_GUI.nv.getChucVu() + " " + TrangChu_GUI.nv.getTenNV());

        // tạo hot key
        paneMain.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                setupKeyEventHandler(newScene);
            }
        });
    }

    private void resetFVe() {
        List<Button> dsbtnFVe = List.of(btnBanVeGUI, btnDoiVeGUI, btnHuyVeGUI);
        dsbtnFVe.forEach(btn -> {
            btn.setStyle(styleLV2);
        });
        iconDown.setIcon(FontAwesomeIcons.ANGLE_DOWN);
        if (acpVe.isVisible()) {
            acpVe.setVisible(false);
            List<Button> dsF = List.of(btnFQLHD, btnFKH, btnFBCTK, btnFCT, btnFKM,btnUM);
            List<FontAwesomeIcon> dsIcon = List.of(iconHD, iconNV, iconHK, iconCT, iconKM, iconTK, iconDownTK, iconHelp);
            dsF.forEach(btn -> {
                btn.setLayoutY(btn.getLayoutY() - acpVe.getHeight());
            });
            dsIcon.forEach(icon -> {
                icon.setLayoutY(icon.getLayoutY() - acpVe.getHeight());
            });
        }
    }

    private void resetFBCTK() {
        List<Button> listBtn = List.of(btnTKDoanhThu, btnTKNV);
        listBtn.forEach(btn -> {
            btn.setStyle(styleLV2);
        });
        iconDownTK.setIcon(FontAwesomeIcons.ANGLE_DOWN);
        if (acpTK.isVisible()) {
            acpTK.setVisible(false);
            btnUM.setLayoutY(btnUM.getLayoutY() - acpTK.getHeight());
            iconHelp.setLayoutY(iconHelp.getLayoutY() - acpTK.getHeight());
        }
    }

    private void resetQLBCTK() {
        List<Button> listBtn = List.of(btnQLTKDoanhThu, btnQLTKNV);
        listBtn.forEach(btn -> {
            btn.setStyle(styleLV2);
        });
        iconDownQLTK.setIcon(FontAwesomeIcons.ANGLE_DOWN);
        if (acpQLTK.isVisible()) {
            acpQLTK.setVisible(false);
            btnUMQL.setLayoutY(btnUMQL.getLayoutY() - acpQLTK.getHeight());
            iconHelp.setLayoutY(iconHelp.getLayoutY() - acpQLTK.getHeight());
        }
    }

    @FXML
    protected void showBanVeGUI() throws RemoteException {
        FXMLLoader loader = new FXMLLoader(TrangChu_GUI.class.getResource("ban-ve.fxml"));
        double width = paneMain.getWidth();
        double height = paneMain.getHeight();
        try {
            paneMain.getChildren().clear();
            paneMain.getChildren().add(loader.load());
            paneMain.setPrefSize(width, height);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<HoaDon> dsHD = hdService.getDSHDLuuTam();
        for (HoaDon hd : dsHD) {
            //Xóa hóa đơn lưu hơn 15 phút
            if (hd.getNgayLapHoaDon().plusMinutes(15).isBefore(LocalDateTime.now())) {
                List<ChiTietHoaDon> dsCTHD = cthdService.getCT_HoaDon(hd.getMaHD());
                for (ChiTietHoaDon cthd : dsCTHD) {
                    if (cthd != null) {
                        Ve ve = cthd.getVe();
                        veService.updateTinhTrangVe(ve.getMaVe(), "DaHuy");
                        VeDTO ve_dto = veService.getVeTheoID(ve.getMaVe());
                        ctltService.updateCTLT(new ChiTietLichTrinh(new ChoNgoi(ve_dto.getMaCho()), new LichTrinh(ve_dto.getMaLichTrinh())), true);
                    }
                }
            }
        }
    }

    @FXML
    protected void showDoiVeGUI() throws RemoteException {
        FXMLLoader loader = new FXMLLoader(TrangChu_GUI.class.getResource("doi-ve.fxml"));
        double width = paneMain.getWidth();
        double height = paneMain.getHeight();
        try {
            paneMain.getChildren().clear();
            paneMain.getChildren().add(loader.load());
            paneMain.setPrefSize(width, height);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<HoaDon> dsHD = hdService.getDSHDLuuTam();
        for (HoaDon hd : dsHD) {
            //Xóa hóa đơn lưu hơn 15 phút
            if (hd.getNgayLapHoaDon().plusMinutes(15).isBefore(LocalDateTime.now())) {
                List<ChiTietHoaDon> dsCTHD = cthdService.getCT_HoaDon(hd.getMaHD());
                for (ChiTietHoaDon cthd : dsCTHD) {
                    if (cthd != null) {
                        Ve ve = cthd.getVe();
                        veService.updateTinhTrangVe(ve.getMaVe(), "DaHuy");
                        VeDTO ve_dto = veService.getVeTheoID(ve.getMaVe());
                        ctltService.updateCTLT(new ChiTietLichTrinh(new ChoNgoi(ve_dto.getMaCho()), new LichTrinh(ve_dto.getMaLichTrinh())), true);
                    }
                }
            }
        }
    }

    @FXML
    protected void showHuyVeGUI() throws RemoteException {
        FXMLLoader loader = new FXMLLoader(TrangChu_GUI.class.getResource("huy-ve.fxml"));
        double width = paneMain.getWidth();
        double height = paneMain.getHeight();
        try {
            paneMain.getChildren().clear();
            paneMain.getChildren().add(loader.load());
            paneMain.setPrefSize(width, height);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<HoaDon> dsHD = hdService.getDSHDLuuTam();
        for (HoaDon hd : dsHD) {
            //Xóa hóa đơn lưu hơn 15 phút
            if (hd.getNgayLapHoaDon().plusMinutes(15).isBefore(LocalDateTime.now())) {
                List<ChiTietHoaDon> dsCTHD = cthdService.getCT_HoaDon(hd.getMaHD());
                for (ChiTietHoaDon cthd : dsCTHD) {
                    if (cthd != null) {
                        Ve ve = VeMapper.toEntity(veService.getVeTheoID(cthd.getVe().getMaVe()));
                        veService.updateTinhTrangVe(ve.getMaVe(), "DaHuy");
                        ctltService.updateCTLT(ve.getChiTietLichTrinh(), true);
                    }
                }
            }
        }
    }

    @FXML
    protected void showHoaDonGUI() {
        FXMLLoader loader = new FXMLLoader(TrangChu_GUI.class.getResource("qly-hoa-don.fxml"));
        double width = paneMain.getWidth();
        double height = paneMain.getHeight();
        paneMain.getChildren().clear();
        try {
            paneMain.getChildren().add(loader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        paneMain.setPrefSize(width, height);
    }

    @FXML
    protected void showNhanVienGUI() {
        FXMLLoader loader = new FXMLLoader(TrangChu_GUI.class.getResource("nhan-vien.fxml"));
        double width = paneMain.getWidth();
        double height = paneMain.getHeight();
        paneMain.getChildren().clear();
        try {
            paneMain.getChildren().add(loader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        paneMain.setPrefSize(width, height);
    }

    @FXML
    protected void showChuyenTauGUI() {
        FXMLLoader loader = new FXMLLoader(TrangChu_GUI.class.getResource("chuyen-tau.fxml"));
        double width = paneMain.getWidth();
        double height = paneMain.getHeight();
        paneMain.getChildren().clear();
        try {
            paneMain.getChildren().add(loader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        paneMain.setPrefSize(width, height);
    }

    @FXML
    protected void showKHGUI() {
        FXMLLoader loader = new FXMLLoader(TrangChu_GUI.class.getResource("khach-hang.fxml"));
        double width = paneMain.getWidth();
        double height = paneMain.getHeight();
        paneMain.getChildren().clear();
        try {
            paneMain.getChildren().add(loader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void showKhuyenMaiGUI() {
        FXMLLoader loader = new FXMLLoader(TrangChu_GUI.class.getResource("khuyen-mai.fxml"));
        double width = paneMain.getWidth();
        double height = paneMain.getHeight();
        paneMain.getChildren().clear();
        try {
            paneMain.getChildren().add(loader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        paneMain.setPrefSize(width, height);
    }

    @FXML
    protected void onClick() {
        FXMLLoader loader = new FXMLLoader(TrangChu_GUI.class.getResource("gioi-thieu.fxml"));
        double width = paneMain.getWidth();
        double height = paneMain.getHeight();
        paneMain.getChildren().clear();
        try {
            paneMain.getChildren().add(loader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        paneMain.setPrefSize(width, height);
    }

    @FXML
    protected void showTKDoanhThuGUI() {
        FXMLLoader loader = new FXMLLoader(TrangChu_GUI.class.getResource("thong-ke-doanh-thu.fxml"));
        double width = paneMain.getWidth();
        double height = paneMain.getHeight();
        paneMain.getChildren().clear();
        try {
            paneMain.getChildren().add(loader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        paneMain.setPrefSize(width, height);
    }

    @FXML
    protected void showTKNhanVienGUI() {
        FXMLLoader loader = new FXMLLoader(TrangChu_GUI.class.getResource("thong-ke-nv.fxml"));
        double width = paneMain.getWidth();
        double height = paneMain.getHeight();
        paneMain.getChildren().clear();
        try {
            paneMain.getChildren().add(loader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        paneMain.setPrefSize(width, height);
    }

    @FXML
    protected void dangXuat() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(null);
        alert.setTitle("Đăng xuất");
        alert.setContentText("Bạn có chắc chắn muốn đăng xuất?");
        alert.showAndWait();
        if (alert.getResult() == ButtonType.OK) {
            getData.nv = null;
            getData.hd = null;
            getData.kh = null;
            getData.lt = null;
            getData.ltkh = null;
            getData.dsve = null;
            getData.dscthd = null;
            System.exit(0);
        }
    }

    @FXML
    protected void openUserManual() {
        try {
            File file = new File(Objects.requireNonNull(getClass().getResource("/QuanLiVeTau/HTML/GioiThieu.html")).toURI());
            Desktop.getDesktop().open(file);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        if (getData.nv.getChucVu().equals("Nhân viên")) {
            resetFVe();
            resetFBCTK();
        } else {
            resetQLBCTK();
        }
        onClick();
    }

    @FXML
    protected void showKetCaPopup() {
        AnchorPane ketCaPane = null;
        try {
            ketCaPane = FXMLLoader.load(Objects.requireNonNull(TrangChu_GUI.class.getResource("ket-ca.fxml")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Stage ketCaStage = new Stage();
        ketCaStage.setTitle("Kết ca");
        ketCaStage.setScene(new Scene(ketCaPane));
        ketCaStage.getIcons().add(new Image("file:src/main/resources/img/logo.png"));
        ketCaStage.show();
        ketCaStage.setOnCloseRequest(e1 -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Xác nhận");
            alert.setHeaderText(null);
            alert.setContentText("Xác nhận thoát?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                ketCaStage.close();
            } else {
                e1.consume();
            }
        });
    }

    @FXML
    protected void openChangePassForm() {
        AnchorPane changePassPane = null;
        try {
            changePassPane = FXMLLoader.load(Objects.requireNonNull(TrangChu_GUI.class.getResource("doi-mat-khau.fxml")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Stage changePassStage = new Stage();
        changePassStage.setTitle("Đổi mật khẩu");
        changePassStage.setScene(new Scene(changePassPane));
        changePassStage.getIcons().add(new Image("file:src/main/resources/img/logo.png"));
        changePassStage.show();
        changePassStage.setOnCloseRequest(e1 -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Xác nhận");
            alert.setHeaderText(null);
            alert.setContentText("Xác nhận thoát?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                changePassStage.close();
            } else {
                e1.consume();
            }
        });
    }

    private void chooseFeatureButton(Button btnChosed) {
        List<Button> dsF = List.of(btnFVe, btnFQLHD, btnFKH, btnFBCTK, btnFCT, btnFKM);
        dsF.forEach(btn -> {
            if (btn.equals(btnChosed)) {
                btn.setStyle(btn.getStyle() + "-fx-background-color: lightgray;-fx-border-color: blue;");
            } else {
                btn.setStyle(style);
            }
        });
    }

    private void chooseFeatureButtonQL(Button btnChosed) {
        List<Button> dsF = List.of(btnQLNV, btnQLHD, btnQLKH, btnQLCT, btnQLBCTK, btnQLKM);
        dsF.forEach(btn -> {
            if (btn.equals(btnChosed)) {
                btn.setStyle(btn.getStyle() + "-fx-background-color: lightgray;-fx-border-color: blue;");
            } else {
                btn.setStyle(style);
            }
        });
    }

    private void chooseFeatureButtonLV2(Button btnChosed) {
        List<Button> dsF = List.of(btnBanVeGUI, btnDoiVeGUI, btnHuyVeGUI, btnTKDoanhThu, btnTKNV);
        dsF.forEach(btn -> {
            if (btn.equals(btnChosed)) {
                btn.setStyle(btn.getStyle() + "-fx-background-color: lightblue;-fx-border-color: white;");
            } else {
                btn.setStyle(styleLV2);
            }
        });
    }

    private void chooseFeatureButtonQLLV2(Button btnChosed) {
        List<Button> dsF = List.of(btnQLTKDoanhThu, btnQLTKNV);
        dsF.forEach(btn -> {
            if (btn.equals(btnChosed)) {
                btn.setStyle(btn.getStyle() + "-fx-background-color: lightblue;-fx-border-color: white;");
            } else {
                btn.setStyle(styleLV2);
            }
        });
    }

    private void setupKeyEventHandler(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (getData.nv.getChucVu().equals("Nhân viên")) {
                if (event.getCode() == KeyCode.F1) {
                    if (!btnFVe.getStyle().contains("lightgray")) {
                        btnFVe.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
                    }
                    btnBanVeGUI.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
                    try {
                        showBanVeGUI();
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (event.getCode() == KeyCode.F2) {
                    if (!btnFVe.getStyle().contains("lightgray")) {
                        btnFVe.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
                    }
                    btnDoiVeGUI.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
                    try {
                        showDoiVeGUI();
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (event.getCode() == KeyCode.F3) {
                    if (!btnFVe.getStyle().contains("lightgray")) {
                        btnFVe.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
                    }
                    btnHuyVeGUI.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
                    try {
                        showHuyVeGUI();
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (event.getCode() == KeyCode.F4) {
                    btnFQLHD.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
                    showHoaDonGUI();
                }
                if (event.getCode() == KeyCode.F5) {
                    btnFKH.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
                    showKHGUI();
                }
                if (event.getCode() == KeyCode.F6) {
                    btnFCT.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
                    showChuyenTauGUI();
                }
                if (event.getCode() == KeyCode.F7) {
                    btnFKM.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
                    showKhuyenMaiGUI();
                }
                if (event.getCode() == KeyCode.F8) {
                    if (!btnFBCTK.getStyle().contains("lightgray")) {
                        btnFBCTK.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
                    }
                    btnTKDoanhThu.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
                    showTKDoanhThuGUI();
                }
                if (event.getCode() == KeyCode.F9) {
                    if (!btnFBCTK.getStyle().contains("lightgray")) {
                        btnFBCTK.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
                    }
                    btnTKNV.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
                    showTKNhanVienGUI();
                }
                if (event.getCode() == KeyCode.ESCAPE) {
                    showKetCaPopup();
                }
                if (event.getCode() == KeyCode.F10) {
                    openUserManual();
                }
            } else {
                if (event.getCode() == KeyCode.F1) {
                    btnQLNV.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
                    showNhanVienGUI();
                }
                if (event.getCode() == KeyCode.F4) {
                    btnQLHD.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
                    showHoaDonGUI();
                }
                if (event.getCode() == KeyCode.F5) {
                    btnQLKH.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
                    showKHGUI();
                }
                if (event.getCode() == KeyCode.F6) {
                    btnQLCT.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
                    showChuyenTauGUI();
                }
                if (event.getCode() == KeyCode.F7) {
                    btnQLKM.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
                    showKhuyenMaiGUI();
                }
                if (event.getCode() == KeyCode.F8) {
                    if (!btnQLBCTK.getStyle().contains("lightgray")) {
                        btnQLBCTK.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
                    }
                    btnQLTKDoanhThu.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
                    showTKDoanhThuGUI();
                }
                if (event.getCode() == KeyCode.F9) {
                    if (!btnQLBCTK.getStyle().contains("lightgray")) {
                        btnQLBCTK.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
                    }
                    btnQLTKNV.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, null, 0, false, false, false, false, false, false, false, false, false, false, null));
                    showTKNhanVienGUI();
                }
                if (event.getCode() == KeyCode.ESCAPE) {
                    dangXuat();
                }
                if (event.getCode() == KeyCode.F10) {
                    openUserManual();
                }
            }
        });
    }
    public void initService() throws MalformedURLException, NotBoundException, RemoteException {
        try {
            lichTrinhService = (LichTrinhService) Naming.lookup("rmi://localhost:7701/lichTrinhService");
            veService = (VeService) Naming.lookup("rmi://localhost:7701/veService");
            ctltService = (CT_LichTrinhService) Naming.lookup("rmi://localhost:7701/ctLichTrinhService");
            cthdService = (CT_HoaDonService) Naming.lookup("rmi://localhost:7701/ctHoaDonService");
            hdService = (HoaDonService) Naming.lookup("rmi://localhost:7701/hoaDonService");
        }catch (NotBoundException | MalformedURLException | RemoteException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize services", e);
        }
    }
}
