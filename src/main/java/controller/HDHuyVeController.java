package controller;

import com.itextpdf.text.DocumentException;
import com.jfoenix.controls.JFXButton;
import entity.*;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import service.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

public class HDHuyVeController implements Initializable {

    @FXML
    private TextField txt_ten;

    @FXML
    private TextField txt_cccd;

    @FXML
    private TextField txt_sdt;

    @FXML
    private TableView<Ve> tbl_dsVe;

    @FXML
    private TableColumn<Ve, String> col_stt;

    @FXML
    private TableColumn<Ve, String> col_hoTen;

    @FXML
    private TableColumn<Ve, String> col_thongTinVe;

    @FXML
    private TableColumn<Ve, String> col_tienVe;

    @FXML
    private TableColumn<Ve, String> col_lePhi;

    @FXML
    private TableColumn<Ve, String> col_tienTra;

    @FXML
    private Label lbl_tongSoVe;

    @FXML
    private Label lbl_tongTienVe;

    @FXML
    private Label lbl_tongLePhi;

    @FXML
    private Label lbl_tongTienTra;

    @FXML
    private Button btn_backTraVe;

    @FXML
    private JFXButton btn_xacNhan;

    private VeService veService;
    private KhachHangService khachHangService;
    private HoaDonService hoaDonService;
    private CT_HoaDonService ctHoaDonService;
    private LoaiVeService loaiVeService;
    private LichTrinhService lichTrinhService;
    private GaService gaService;

    private int tongSoVe = 0;
    private double tongTienVe = 0;
    private double tongLePhi = 0;
    private double tongTienTra = 0;

    private ArrayList<Ve> listVe;
    private HashMap<String, Double> mapLePhi;
    private final PrintPDF printPDF = new PrintPDF();
    private final NumberFormat currencyVN = NumberFormat.getCurrencyInstance(Locale.of("vi", "VN"));

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // TODO
        initServices();
        initComponent();

        btn_backTraVe.setOnAction(actionEvent -> {
            // đóng cửa số trả vé
            Scene scene = btn_backTraVe.getScene();
            scene.getWindow().hide();

            lamMoi();
        });

        btn_xacNhan.setOnAction(actionEvent -> {
            if (checkInput()) {
                String ten = txt_ten.getText();
                String cccd = txt_cccd.getText();
                String sdt = txt_sdt.getText();

                KhachHang khHuyVe = null;
                try {
                    khHuyVe = khachHangService.getKHTheoCCCD(cccd);
                    if (khHuyVe == null) {
                        khHuyVe = khachHangService.getKhachHangTheoSDT(sdt);
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }

                if (!kiemTraKH(khHuyVe)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Lỗi");
                    alert.setHeaderText("Không thể hủy vé");
                    alert.setContentText("Khách hàng không trùng khách hàng đã mua vé hoặc trùng thông tin hành khách trên vé");
                    alert.showAndWait();
                    return;
                }

                HoaDon hoaDon = new HoaDon();
                hoaDon.setKhachHang(khHuyVe);
                hoaDon.setNhanVien(getData.nv);
                hoaDon.setNgayLapHoaDon(LocalDateTime.now());
                hoaDon.setTrangThai(false);

                try {
                    if (hoaDonService.createTempInvoice(hoaDon)) {
                        getData.hd = hoaDonService.getHoaDonVuaTao();
                    } else {
                        // thông báo
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Lỗi");
                        alert.setHeaderText("Không thể tạo hóa đơn");
                        alert.showAndWait();
                        return;
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }

                hoaDon = getData.hd;
                hoaDon.setTrangThai(true);
                hoaDon.setTongTien(0 - tongTienTra);
                hoaDon.setTongGiamGia(tongLePhi); // tổng giảm giá = tổng lệ phí
                hoaDon.setKhuyenMai(new KhuyenMai(null));

                try {
                    if (hoaDonService.update(hoaDon)) {
                        try {
                            for (Ve ve : listVe) {
                                ChiTietHoaDon ctHoaDon = new ChiTietHoaDon();
                                ctHoaDon.setHoaDon(new HoaDon(hoaDon.getMaHD()));
                                ctHoaDon.setVe(ve);
                                ChiTietHoaDon ctHoaDonCu = ctHoaDonService.getCT_HoaDonTheoMaVe(ve.getMaVe());
                                ctHoaDon.setGiaVe(0 - ctHoaDonCu.getGiaVe()); // giá vé = giá vé cũ
                                ctHoaDon.setGiaGiam(mapLePhi.get(ve.getMaVe())); // giảm giá = lệ phí
                                if (ctHoaDonService.create(ctHoaDon)) {
                                    ve.setTinhTrangVe("DaHuy");
                                    veService.update(ve);
                                } else {
                                    // thông báo
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setTitle("Lỗi");
                                    alert.setHeaderText("Không thể cập nhật chi tiết hóa đơn");
                                    alert.showAndWait();
                                    return;
                                }
                                printPDF.inHDHuy(hoaDon);
                                lamMoi();
                            }
                        } catch (IOException | DocumentException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        // thông báo
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Lỗi");
                        alert.setHeaderText("Không thể cập nhật hóa đơn");
                        alert.showAndWait();
                        return;
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }


                // đóng cửa số trả vé
                Scene scene = btn_xacNhan.getScene();
                scene.getWindow().hide();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText("Dữ liệu không hợp lệ");
                alert.setContentText("Vui lòng kiểm tra lại thông tin khách hàng");
                alert.showAndWait();
            }
        });

        txt_cccd.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                searchKhachHang();
            }
        });
        txt_sdt.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                searchKhachHang();
            }
        });

    }

    private void initComponent() {
        listVe = getData.dsve;
        mapLePhi = getData.mapLePhi;


        renderTable();
        renderLabel();
    }


    private void renderTable() {
        tongSoVe = listVe.size();

        ObservableList<Ve> list = FXCollections.observableArrayList(listVe);
        tbl_dsVe.setItems(list);
        tbl_dsVe.setItems(list);
        col_stt.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(tbl_dsVe.getItems().indexOf(p.getValue()) + 1 + ""));
        col_hoTen.setCellValueFactory(p -> {
            String ten = p.getValue().getTenKH();
            return new SimpleStringProperty(ten);
        });

        col_thongTinVe.setCellValueFactory(p -> {
            Ve ve = null;
            try {
                ve = veService.getVeTheoID(p.getValue().getMaVe());
                LoaiVe lv = loaiVeService.getLoaiVeTheoMa(ve.getLoaiVe().getMaLoaiVe());
                LichTrinh lt = lichTrinhService.getLichTrinhTheoID(ve.getChiTietLichTrinh().getLichTrinh().getMaLichTrinh());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                return new SimpleStringProperty(gaService.getGaTheoMaGa(lt.getGaDi().getMaGa()).getTenGa() + " - " +
                        gaService.getGaTheoMaGa(lt.getGaDen().getMaGa()).getTenGa() + " \n" +
                        "Giờ khởi hành: \n" + lt.getThoiGianKhoiHanh().format(formatter) + " \n" +
                        lv.getTenLoaiVe());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });

        col_tienVe.setCellValueFactory(p -> {
            Ve ve = null;
            ChiTietHoaDon ctHoaDon = null;
            try {
                ve = veService.getVeTheoID(p.getValue().getMaVe());
                ctHoaDon = ctHoaDonService.getCT_HoaDonTheoMaVe(ve.getMaVe());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            return new SimpleStringProperty(currencyVN.format(ctHoaDon.getGiaVe() - ctHoaDon.getGiaGiam()));
        });

        col_lePhi.setCellValueFactory(p -> {
            Ve ve = null;
            try {
                ve = veService.getVeTheoID(p.getValue().getMaVe());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            return new SimpleStringProperty(currencyVN.format(mapLePhi.get(ve.getMaVe())));
        });

        col_tienTra.setCellValueFactory(p -> {
            Ve ve = null;
            try {
                ve = veService.getVeTheoID(p.getValue().getMaVe());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            ChiTietHoaDon ctHoaDon = null;
            try {
                ctHoaDon = ctHoaDonService.getCT_HoaDonTheoMaVe(ve.getMaVe());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            return new SimpleStringProperty(currencyVN.format(ctHoaDon.getGiaVe() - ctHoaDon.getGiaGiam() - mapLePhi.get(ve.getMaVe())));
        });
    }

    private void renderLabel() {

        for (Ve ve : listVe) {
            ChiTietHoaDon ctHoaDon = null;
            try {
                ctHoaDon = ctHoaDonService.getCT_HoaDonTheoMaVe(ve.getMaVe());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            tongTienVe += ctHoaDon.getGiaVe() - ctHoaDon.getGiaGiam();
            tongLePhi += mapLePhi.get(ve.getMaVe());
            tongTienTra += ctHoaDon.getGiaVe() - ctHoaDon.getGiaGiam() - mapLePhi.get(ve.getMaVe());
        }

        tongTienVe = Math.round(tongTienVe * 1000.0) / 1000.0;
        tongLePhi = Math.round(tongLePhi * 1000.0) / 1000.0;
        tongTienTra = Math.round(tongTienTra * 1000.0) / 1000.0;

        lbl_tongSoVe.setText("Tổng số vé: " + tongSoVe);
        lbl_tongTienVe.setText("Tổng tiền vé: " + currencyVN.format(tongTienVe));
        lbl_tongLePhi.setText("Tổng lệ phí: " + currencyVN.format(tongLePhi));
        lbl_tongTienTra.setText("Tổng tiền trả: " + currencyVN.format(tongTienTra));
    }

    private boolean checkInput() {
        String cccd = txt_cccd.getText();
        String sdt = txt_sdt.getText();

        if (cccd.isEmpty() || sdt.isEmpty()) {
            return false;
        }

        if(!cccd.matches("[0-9]{9}|[0-9]{12}")) {
            return false;
        }

        return sdt.matches("[0-9]{10}|[0-9]{11}");
    }

    private void searchKhachHang() {
        String cccd = txt_cccd.getText();
        String sdt = txt_sdt.getText();

        if (!cccd.isEmpty() || !sdt.isEmpty()) {
            KhachHang kh = null;
            try {
                kh = khachHangService.getKHTheoCCCD(cccd);
                if (kh == null) {
                    kh = khachHangService.getKhachHangTheoSDT(sdt);
                }
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }

            if (kh != null) {
                txt_ten.setText(kh.getTenKH());
                txt_sdt.setText(kh.getSdt());
                txt_cccd.setText(kh.getSoCCCD());
            } else {
                txt_ten.clear();
                txt_sdt.clear();
                txt_cccd.clear();

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Không tìm thấy hành khách");
            }
        }
    }

    private boolean kiemTraKH(KhachHang kh) {
    if (kh == null) {;
        return false;
    }

    ChiTietHoaDon ctHoaDon = null;
    KhachHang tmp = null;
    try {
        ctHoaDon = ctHoaDonService.getCT_HoaDonTheoMaVe(listVe.getFirst().getMaVe());
        HoaDon hd = hoaDonService.getHoaDonTheoMa(ctHoaDon.getHoaDon().getMaHD());
        tmp = khachHangService.getKhachHangTheoMaKH(hd.getKhachHang().getMaKH());
    } catch (RemoteException e) {
        throw new RuntimeException(e);
    }

    if (!tmp.getMaKH().equals(kh.getMaKH())) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Không trùng khách hàng");
        return true;
    }

    return listVe.stream()
            .map(ve -> {
                try {
                    return khachHangService.getKhachHangTheoMaKH(ve.getKhachHang().getMaKH());
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            })
            .anyMatch(tmpKH -> tmpKH.getMaKH().equals(kh.getMaKH()));
}

    private void initServices() {
        try {
            veService = (VeService) Naming.lookup("rmi://localhost:7701/veService");
            khachHangService = (KhachHangService) Naming.lookup("rmi://localhost:7701/khachHangService");
            hoaDonService = (HoaDonService) Naming.lookup("rmi://localhost:7701/hoaDonService");
            ctHoaDonService = (CT_HoaDonService) Naming.lookup("rmi://localhost:7701/ctHoaDonService");
            loaiVeService = (LoaiVeService) Naming.lookup("rmi://localhost:7701/loaiVeService");
            lichTrinhService = (LichTrinhService) Naming.lookup("rmi://localhost:7701/lichTrinhService");
            gaService = (GaService) Naming.lookup("rmi://localhost:7701/gaService");
            khachHangService = (KhachHangService) Naming.lookup("rmi://localhost:7701/khachHangService");
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize services", e);
        }
        listVe = getData.dsve;
        mapLePhi = getData.mapLePhi;
    }

    private void lamMoi() {
        txt_ten.clear();
        txt_cccd.clear();
        txt_sdt.clear();
    }
}
