/*
 * @ (#) QLyHoaDonController.java       1.0     20/10/2024
 *
 *Copyright (c) 2024 IUH. All right reserved.
 */
package controller;

import com.itextpdf.text.DocumentException;
import entity.*;
import gui.TrangChu_GUI;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import service.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/*
 * @description:
 * @author: Hiep Nguyen
 * @date:   20/10/2024
 * version: 1.0
 */
public class QLyHoaDonController implements Initializable {

    @FXML
    private Button btnChiTiet;

    @FXML
    private Button btnInHD;

    @FXML
    private Button btnInLaiVe;

    @FXML
    private Button btnThanhToan;

    @FXML
    private Button btnLamMoi;

    @FXML
    private ComboBox<String> cbLoaiVe;

    @FXML
    private TableColumn<ChiTietHoaDon, String> colGiaGiam;


    @FXML
    private TableColumn<ChiTietHoaDon, String> colTongTienVe;

    @FXML
    private TableColumn<ChiTietHoaDon, String> colGiaVe;

    @FXML
    private TableColumn<ChiTietHoaDon, String> colLoaiCho;

    @FXML
    private TableColumn<ChiTietHoaDon, String> colLoaiVe;

    @FXML
    private TableColumn<HoaDon, String> colMaHD;

    @FXML
    private TableColumn<HoaDon, String> colNgayLapHD;

    @FXML
    private TableColumn<HoaDon, Integer> colSLVe;

    @FXML
    private TableColumn<HoaDon, Integer> colSTT;

    @FXML
    private TableColumn<ChiTietHoaDon, Integer> colSTT1;

    @FXML
    private TableColumn<ChiTietHoaDon, String> colTTVe;

    @FXML
    private TableColumn<HoaDon, String> colThanhTien;

    @FXML
    private TableColumn<HoaDon, String> colKH;

    @FXML
    private TableColumn<HoaDon, String> colTienGiam;

    @FXML
    private TableColumn<HoaDon, String> colTienThue;

    @FXML
    private TableColumn<HoaDon, String> colTienVe;

    @FXML
    private DatePicker dpNgayLapHD;

    @FXML
    private ToggleGroup hoaDon;

    @FXML
    private RadioButton radioAllHD;

    @FXML
    private RadioButton radioHDLuuTam;

    @FXML
    private RadioButton radioHDTrongNgay;

    @FXML
    private TableView<ChiTietHoaDon> tbCTHD;

    @FXML
    private TableView<HoaDon> tbhd;

    @FXML
    private TextField txtGiaGiam;

    @FXML
    private TextField txtGiaVe;

    @FXML
    private TextField txtMaHD;

    @FXML
    private TextField txtMaVe;

    @FXML
    private TextField txtTenHK;

    @FXML
    private TextField txtTenKH;

    @FXML
    private TextField txtThanhTienVe;

    @FXML
    private TextField txtTienGiam;

    @FXML
    private TextField txtTienThue;

    @FXML
    private TextField txtTimKiem;

    @FXML
    private TextField txtTongTien;


    private List<HoaDon> listHD = new ArrayList<>();
    private List<ChiTietHoaDon> listCTHD = new ArrayList<>();


    private VeService veService;
    private GaService gaService;
    private CT_LichTrinhService ctltService;
    private CT_HoaDonService cthdService;
    private HoaDonService hdService;
    private ToaService toaService;
    private LoaiToaService ltoaService;
    private ChoNgoiService cnService;
    private LoaiVeService lvService;
    private LichTrinhService lichTrinhService;
    private KhachHangService khService;


    @SneakyThrows
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initService();
        NumberFormat nf = DecimalFormat.getCurrencyInstance(Locale.of("vi", "VN"));
        cbLoaiVe.getItems().addAll("Người lớn", "Trẻ em", "Học sinh, sinh viên", "Người cao tuổi");
        //Bảng cthd
        colSTT1.setCellValueFactory(p -> new SimpleIntegerProperty(tbCTHD.getItems().indexOf(p.getValue()) + 1).asObject());
        colTTVe.setCellValueFactory(param -> {
            Ve ve = param.getValue().getVe();
            LichTrinh lt = null;
            try {
                lt = lichTrinhService.getLichTrinhTheoID(veService.getVeTheoID(ve.getMaVe()).getChiTietLichTrinh().getLichTrinh().getMaLichTrinh());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            try {
                return new SimpleStringProperty(  lt.getSoHieuTau().getSoHieuTau()+ " - " + gaService.getGaTheoMaGa(lt.getGaDi().getMaGa()).getTenGa() + " - " + gaService.getGaTheoMaGa(lt.getGaDen().getMaGa()).getTenGa() + "\n" + formatter.format(lt.getThoiGianKhoiHanh()) + " - " + formatter.format(lt.getThoiGianDuKienDen()));
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });

        colLoaiCho.setCellValueFactory(param -> {
            Ve ve = param.getValue().getVe();
            ChoNgoi cn = null;
            try {
                cn = cnService.getChoNgoiTheoMa(veService.getVeTheoID(ve.getMaVe()).getChiTietLichTrinh().getChoNgoi().getMaCho());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            try {
                return new SimpleStringProperty(ltoaService.getLoaiToaTheoMa(toaService.getToaTheoID(cn.getToa().getMaToa()).getLoaiToa().getMaLoaiToa()).getTenLoaiToa());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });

        colLoaiVe.setCellValueFactory(param -> {
            Ve ve = null;
            try {
                ve = veService.getVeTheoID(param.getValue().getVe().getMaVe());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }

            try {
                return new SimpleStringProperty(lvService.getLoaiVeTheoMa(ve.getLoaiVe().getMaLoaiVe()).getTenLoaiVe());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });

        colGiaVe.setCellValueFactory(param -> {
            Ve ve = null;
            try {
                ve = veService.getVeTheoID(param.getValue().getVe().getMaVe());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            ChiTietLichTrinh ctlt = null;
            try {
                ctlt = ctltService.getCTLTTheoCN(ve.getChiTietLichTrinh().getLichTrinh().getMaLichTrinh(), ve.getChiTietLichTrinh().getChoNgoi().getMaCho());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            return new SimpleStringProperty(nf.format(ctlt.getGiaCho()));
        });

        colGiaGiam.setCellValueFactory(param -> new SimpleStringProperty(nf.format(param.getValue().getGiaGiam())));

        colTongTienVe.setCellValueFactory(param -> new SimpleStringProperty(nf.format(param.getValue().getGiaVe())));
        tbCTHD.setOnMouseClicked(event -> {
            ChiTietHoaDon cthd = tbCTHD.getSelectionModel().getSelectedItem();
            txtMaVe.setText(cthd.getVe().getMaVe());
            try {
                txtTenHK.setText(veService.getVeTheoID(cthd.getVe().getMaVe()).getTenHanhKhach());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            try {
                cbLoaiVe.setValue(lvService.getLoaiVeTheoMa(veService.getVeTheoID(cthd.getVe().getMaVe()).getLoaiVe().getMaLoaiVe()).getTenLoaiVe());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            txtGiaVe.setText(nf.format(cthd.getGiaVe()));
            txtGiaGiam.setText(nf.format(cthd.getGiaGiam()));
            txtThanhTienVe.setText(nf.format(cthd.getGiaVe() - cthd.getGiaGiam()));
        });
        //Bảng hd
        colSTT.setCellValueFactory(p -> new SimpleIntegerProperty(tbhd.getItems().indexOf(p.getValue()) + 1).asObject());
        colMaHD.setCellValueFactory(new PropertyValueFactory<>("maHD"));
        colNgayLapHD.setCellValueFactory(p -> new SimpleStringProperty(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(p.getValue().getNgayLapHoaDon())));
        colKH.setCellValueFactory(p -> {
            try {
                return new SimpleStringProperty(khService.getKhachHangTheoMaKH(p.getValue().getKhachHang().getMaKH()).getTenKH());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
        colSLVe.setCellValueFactory(p -> {
            try {
                listCTHD = cthdService.getCT_HoaDon(p.getValue().getMaHD());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            return new SimpleIntegerProperty(listCTHD.size()).asObject();
        });
        colTienVe.setCellValueFactory(p -> {
            try {
                listCTHD = cthdService.getCT_HoaDon(p.getValue().getMaHD());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            double tongTien = 0;
            for (ChiTietHoaDon cthd : listCTHD) {
                tongTien += cthd.getGiaVe();
            }
            return new SimpleStringProperty(nf.format(tongTien));
        });
        colTienGiam.setCellValueFactory(p -> new SimpleStringProperty(nf.format(p.getValue().getTongGiamGia())));
        colTienThue.setCellValueFactory(p -> new SimpleStringProperty(nf.format(p.getValue().getTongTien() * 0.1)));
        colThanhTien.setCellValueFactory(p -> new SimpleStringProperty(nf.format(p.getValue().getTongTien())));
        tbhd.setOnMouseClicked(event -> {
            HoaDon hd = tbhd.getSelectionModel().getSelectedItem();
            if (hd != null) {
                txtMaHD.setText(hd.getMaHD());
                try {
                    txtTenKH.setText(khService.getKhachHangTheoMaKH(hd.getKhachHang().getMaKH()).getTenKH());
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                dpNgayLapHD.setValue(hd.getNgayLapHoaDon().toLocalDate());
                txtTongTien.setText(nf.format(hd.getTongTien()));
                txtTienGiam.setText(nf.format(hd.getTongGiamGia()));
                txtTienThue.setText(nf.format(hd.getTongTien() * 0.1));
                btnChiTiet.setDisable(false);
                btnInHD.setDisable(false);
                if (radioHDLuuTam.isSelected()) {
                    btnThanhToan.setDisable(false);
                    btnInHD.setDisable(true);
                } else {
                    btnThanhToan.setDisable(true);
                }
            }
            tbCTHD.getItems().clear();
            txtMaVe.clear();
            txtTenHK.clear();
            txtGiaVe.clear();
            txtGiaGiam.clear();
            txtThanhTienVe.clear();
            btnInLaiVe.setDisable(true);
        });

        txtTimKiem.setOnKeyReleased(event -> {
            if (txtTimKiem.getText() != null && !txtTimKiem.getText().isEmpty()) {
                radioAllHD.setDisable(false);
                radioHDLuuTam.setDisable(true);
                radioHDTrongNgay.setDisable(false);
            } else {
                radioAllHD.setDisable(true);
                radioHDLuuTam.setDisable(false);
                radioHDTrongNgay.setDisable(false);
            }
            if (event.getCode().toString().equals("ENTER")) {
                try {
                    timKiemHD(null);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        radioHDLuuTam.setOnAction(event -> {
            lamMoi();
            try {
                listHD = hdService.getDSHDLuuTam();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            listHD.removeIf(hd -> hd.getNgayLapHoaDon().plusMinutes(15).isBefore(LocalDateTime.now()));
            tbhd.getItems().clear();
            tbhd.getItems().addAll(listHD);
        });

        radioHDTrongNgay.setOnAction(event -> {
            if (txtTimKiem.getText() == null || txtTimKiem.getText().isEmpty())  {
                lamMoi();
                try {
                    listHD = hdService.getDSHDTheoNgay(LocalDate.from(LocalDateTime.now()));
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                listHD.removeIf(hd -> !hd.isTrangThai());
                tbhd.getItems().clear();
                tbhd.getItems().addAll(listHD);
            }
        });

        btnInHD.setOnAction(event -> {
            HoaDon hd = tbhd.getSelectionModel().getSelectedItem();
            List<ChiTietHoaDon> dscthd = null;
            try {
                dscthd = cthdService.getCT_HoaDon(hd.getMaHD());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            PrintPDF printPDF = new PrintPDF();

            if (hd.getTongTien() <= 0) {
                for (ChiTietHoaDon cthd : dscthd) {
                    Ve ve = null;
                    try {
                        ve = veService.getVeTheoID(cthd.getVe().getMaVe());
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                    if (ve.getTinhTrangVe().equals("DaDoi")) {
                        try {
                            printPDF.inHoaDon(hd);
                            return;
                        } catch (IOException | DocumentException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                try {
                    printPDF.inHDHuy(hd);
                } catch (IOException | DocumentException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    printPDF.inHoaDon(hd);
                } catch (IOException | DocumentException e) {
                    throw new RuntimeException(e);
                }
            }

        });

        // bấm enter để tìm kiếm
        txtTimKiem.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                try {
                    timKiemHD(null);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        btnInLaiVe.setOnAction(event -> {
            ArrayList<Ve> dsve = new ArrayList<>();
            ChiTietHoaDon cthd = tbCTHD.getSelectionModel().getSelectedItem();
            getData.hd = tbhd.getSelectionModel().getSelectedItem();
            if (cthd == null) {
                for (ChiTietHoaDon cthd1 : listCTHD) {
                    Ve ve = null;
                    try {
                        ve = veService.getVeTheoID(cthd1.getVe().getMaVe());
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                    dsve.add(ve);
                }
            } else {
                Ve ve = null;
                try {
                    ve = veService.getVeTheoID(cthd.getVe().getMaVe());
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                dsve.add(ve);
            }
            try {
                new PrintPDF().inVe(new ArrayList<>(dsve));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        btnChiTiet.setOnAction(event -> {
            HoaDon hd = tbhd.getSelectionModel().getSelectedItem();
            try {
                listCTHD = cthdService.getCT_HoaDon(hd.getMaHD());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            tbCTHD.getItems().clear();
            tbCTHD.getItems().addAll(listCTHD);
            if (hd.isTrangThai()) {
                btnInLaiVe.setDisable(false);
            }
        });

        btnThanhToan.setOnAction(event -> {
            HoaDon hd = tbhd.getSelectionModel().getSelectedItem();
            getData.hd = hd;
            try {
                getData.kh = khService.getKhachHangTheoMaKH(hd.getKhachHang().getMaKH());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            List<ChiTietHoaDon> dscthd = null;
            try {
                dscthd = cthdService.getCT_HoaDon(hd.getMaHD());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            getData.dscthd = dscthd;
            ArrayList<Ve> dsve = new ArrayList<>();
            for (ChiTietHoaDon cthd : dscthd) {
                Ve ve = null;
                try {
                    ve = veService.getVeTheoID(cthd.getVe().getMaVe());
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                try {
                    ve.setChiTietLichTrinh(ctltService.getCTLTTheoCN(ve.getChiTietLichTrinh().getLichTrinh().getMaLichTrinh(), ve.getChiTietLichTrinh().getChoNgoi().getMaCho()));
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                try {
                    ve.setLoaiVe(lvService.getLoaiVeTheoMa(ve.getLoaiVe().getMaLoaiVe()));
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                dsve.add(ve);
                cthd.setVe(ve);
            }
            getData.dsve = dsve;
            try {
                AnchorPane acpHoaDon = FXMLLoader.load(Objects.requireNonNull(TrangChu_GUI.class.getResource("hoa-don.fxml")));
                Stage stgHoaDon = new Stage();
                stgHoaDon.setTitle("Thanh toán");
                stgHoaDon.getIcons().add(new Image("file:src/main/resources/img/logo.png"));
                stgHoaDon.setScene(new Scene(acpHoaDon));
                stgHoaDon.sizeToScene();
                stgHoaDon.show();
                Button btnLuuTam = (Button) stgHoaDon.getScene().lookup("#btnLuuTamHD");
                btnLuuTam.setDisable(true);
                stgHoaDon.setOnCloseRequest(e1 -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Xác nhận");
                    alert.setHeaderText(null);
                    alert.setContentText("Xác nhận thoát?");
                    alert.showAndWait();
                    if (alert.getResult().getText().equals("OK")) {
                        stgHoaDon.close();
                        radioAllHD.setSelected(true);
                        lamMoi();
                        if (getData.hd.isTrangThai()) {
                            btnInHD.setDisable(false);
                        }
                    }
                });

                Button btnBack = (Button) acpHoaDon.lookup("#btnBackBanVe");
                btnBack.setOnMouseClicked(e1 ->
                    {
                        stgHoaDon.close();
                        if (getData.hd.isTrangThai()) {
                            radioHDTrongNgay.setSelected(true);
                            radioHDTrongNgay.fire();
                            tbhd.getSelectionModel().select(getData.hd);
                            btnThanhToan.setDisable(true);
                            btnInHD.setDisable(false);
                            btnChiTiet.fire();
                        }
                    });
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        btnLamMoi.setOnAction(event -> {
            lamMoi();
            //hủy chọn tất cả radio button
            radioAllHD.setSelected(true);
        });
    }

    protected void lamMoi() {
        txtMaHD.clear();
        txtTenKH.clear();
        txtTongTien.clear();
        txtTienGiam.clear();
        txtTienThue.clear();
        txtTimKiem.clear();
        txtMaVe.clear();
        txtTenHK.clear();
        txtGiaVe.clear();
        txtGiaGiam.clear();
        txtThanhTienVe.clear();
        dpNgayLapHD.setValue(null);
        cbLoaiVe.setValue(cbLoaiVe.getPromptText());
        tbhd.getItems().clear();
        tbCTHD.getItems().clear();
        btnChiTiet.setDisable(true);
        btnInHD.setDisable(true);
        btnThanhToan.setDisable(true);
        btnInLaiVe.setDisable(true);
        txtTimKiem.requestFocus();
        radioAllHD.setDisable(true);
        radioHDLuuTam.setDisable(false);
        radioHDTrongNgay.setDisable(false);
    }

    @FXML
    protected void timKiemHD(MouseEvent event) throws RemoteException {
        String ma = txtTimKiem.getText();
        // tim kiem hoa don theo ma
        if (ma == null || ma.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng nhập mã hóa đơn");
            alert.show();
            txtTimKiem.requestFocus();
        } else {
            if (radioHDTrongNgay.isSelected()) {
                HoaDon hd = hdService.getHoaDonTheoMa(ma);
                if (hd == null) {
                    listHD = hdService.getHoaDonTheoKH(ma);
                    listHD.removeIf(hd1 -> !hd1.getNgayLapHoaDon().toLocalDate().isEqual(LocalDateTime.now().toLocalDate()));
                    if (listHD.isEmpty()) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Lỗi");
                        alert.setHeaderText(null);
                        alert.setContentText("Không tìm thấy hóa đơn");
                        alert.show();
                        lamMoi();
                    } else {
                        lamMoi();
                        tbhd.getItems().addAll(listHD);
                    }
                } else {
                    lamMoi();
                    if (hd.getNgayLapHoaDon().toLocalDate().isEqual(LocalDateTime.now().toLocalDate())) {
                        tbhd.getItems().add(hd);
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Lỗi");
                        alert.setHeaderText(null);
                        alert.setContentText("Không tìm thấy hóa đơn");
                        alert.show();
                        lamMoi();
                        radioAllHD.setSelected(true);
                    }
                }
            } else {
                HoaDon hd = hdService.getHoaDonTheoMa(ma);
                if (hd == null) {
                    listHD = hdService.getHoaDonTheoKH(ma);
                    if (listHD.isEmpty()) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Lỗi");
                        alert.setHeaderText(null);
                        alert.setContentText("Không tìm thấy hóa đơn");
                        alert.show();
                        lamMoi();
                    } else {
                        lamMoi();
                        tbhd.getItems().addAll(listHD);
                    }
                } else {
                    lamMoi();
                    tbhd.getItems().addAll(listHD);
                }
            }
        }
    }
    public void initService() throws MalformedURLException, NotBoundException, RemoteException {
        try {
            lichTrinhService = (LichTrinhService) Naming.lookup("rmi://localhost:7701/lichTrinhService");
            veService = (VeService) Naming.lookup("rmi://localhost:7701/veService");
            gaService = (GaService) Naming.lookup("rmi://localhost:7701/gaService");
            ctltService = (CT_LichTrinhService) Naming.lookup("rmi://localhost:7701/ctLichTrinhService");
            cthdService = (CT_HoaDonService) Naming.lookup("rmi://localhost:7701/ctHoaDonService");
            hdService = (HoaDonService) Naming.lookup("rmi://localhost:7701/hoaDonService");
            toaService = (ToaService) Naming.lookup("rmi://localhost:7701/toaService");
            ltoaService = (LoaiToaService) Naming.lookup("rmi://localhost:7701/loaiToaService");
            cnService = (ChoNgoiService) Naming.lookup("rmi://localhost:7701/choNgoiService");
            lvService = (LoaiVeService) Naming.lookup("rmi://localhost:7701/loaiVeService");
            khService = (KhachHangService) Naming.lookup("rmi://localhost:7701/khachHangService");
        }catch (NotBoundException | MalformedURLException | RemoteException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize services", e);
        }
    }
}
