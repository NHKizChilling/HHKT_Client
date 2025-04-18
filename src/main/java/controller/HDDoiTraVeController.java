package controller;

import com.itextpdf.text.DocumentException;
import com.jfoenix.controls.JFXButton;
import dao.*;
import entity.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HDDoiTraVeController implements Initializable {
    @FXML
    private AnchorPane acpTTHD;

    @FXML
    private Button btnBackBanVe;

    @FXML
    private Button btnGia1;

    @FXML
    private Button btnGia2;

    @FXML
    private Button btnGia3;

    @FXML
    private Button btnGia4;

    @FXML
    private JFXButton btnInHD;

    @FXML
    private JFXButton btnThanhToan;

    @FXML
    private TableColumn<ChiTietHoaDon, String> colGiaGiam;

    @FXML
    private TableColumn<ChiTietHoaDon, String> colGiaVe;

    @FXML
    private TableColumn<ChiTietHoaDon, String> colLoaiCho;

    @FXML
    private TableColumn<ChiTietHoaDon, String> colLoaiVe;

    @FXML
    private TableColumn<ChiTietHoaDon, Integer> colSTT;

    @FXML
    private TableColumn<ChiTietHoaDon, String> colTTVe;

    @FXML
    private TableColumn<ChiTietHoaDon, String> colThanhTien;

    @FXML
    private TableView<ChiTietHoaDon> tbCTHD;

    @FXML
    private TextField txtKH;

    @FXML
    private TextField txtMaHD;

    @FXML
    private TextField txtMaNV;

    @FXML
    private TextField txtNgayLapHD;

    @FXML
    private TextField txtSDT;

    @FXML
    private TextField txtTenNV;

    @FXML
    private TextField txtThanhTien;

    @FXML
    private TextField txtTienKH;

    @FXML
    private TextField txtTienTra;

    @FXML
    private Label lblThanhTien;

    private final CT_LichTrinh_DAO ctl_dao = new CT_LichTrinh_DAO();
    private final HoaDon_DAO hd_dao = new HoaDon_DAO();
    private final KhuyenMai_DAO km_dao = new KhuyenMai_DAO();
    private final CT_HoaDon_DAO cthd_dao = new CT_HoaDon_DAO();
    private double tongTien = 0;
    private final Ve_DAO ve_dao = new Ve_DAO();
    private HoaDon hd;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // TODO
        NumberFormat df = DecimalFormat.getCurrencyInstance(Locale.of("vi", "VN"));
        NhanVien nhanVien = getData.nv;
        txtMaNV.setText(nhanVien.getMaNhanVien());
        txtTenNV.setText(nhanVien.getTenNhanVien());
        txtMaHD.setText(getData.hd.getMaHoaDon());
        txtNgayLapHD.setText(getData.hd.getNgayLapHoaDon().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        txtKH.setText(getData.kh.getTenKH());
        txtSDT.setText(getData.kh.getSdt());
        hd = getData.hd;
        hd.setKhuyenMai(km_dao.getKMGiamCaoNhat());
        Ve ve_doi = getData.dsve.getFirst();
        ChiTietHoaDon cthd = new ChiTietHoaDon(hd, ve_doi);

        txtTienTra.setOnKeyReleased(event -> {
            goiYGia();
        });

        colSTT.setCellValueFactory(column -> new SimpleIntegerProperty(tbCTHD.getItems().indexOf(column.getValue()) + 1).asObject());

        colTTVe.setCellValueFactory(param -> {
            LichTrinh lt1 = new LichTrinh_DAO().getLichTrinhTheoID(ve_doi.getCtlt().getLichTrinh().getMaLichTrinh());
            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return new SimpleStringProperty(  lt1.getChuyenTau().getSoHieutau()+ " - " + new Ga_DAO().getGaTheoMaGa(lt1.getGaDi().getMaGa()).getTenGa() + " - " + new Ga_DAO().getGaTheoMaGa(lt1.getGaDen().getMaGa()).getTenGa() + "\n" + formatter1.format(lt1.getThoiGianKhoiHanh()) + " - " + formatter1.format(lt1.getThoiGianDuKienDen()));
        });

        colLoaiCho.setCellValueFactory(param -> {
            ChoNgoi cn1 = new ChoNgoi_DAO().getChoNgoiTheoMa(ve_doi.getCtlt().getChoNgoi().getMaChoNgoi());
            return new SimpleStringProperty(new LoaiToa_DAO().getLoaiToaTheoMa(new Toa_DAO().getToaTheoID(cn1.getToa().getMaToa()).getLoaiToa().getMaLoaiToa()).getTenLoaiToa());
        });

        colLoaiVe.setCellValueFactory(param -> {
            LoaiVe_DAO lv_dao = new LoaiVe_DAO();
            return new SimpleStringProperty(lv_dao.getLoaiVeTheoMa(ve_doi.getLoaiVe().getMaLoaiVe()).getTenLoaiVe());
        });

        colGiaVe.setCellValueFactory(param -> {
            ChiTietLichTrinh ctlt = new CT_LichTrinh_DAO().getCTLTTheoCN(ve_doi.getCtlt().getLichTrinh().getMaLichTrinh(), ve_doi.getCtlt().getChoNgoi().getMaChoNgoi());
            return new SimpleStringProperty(df.format(ctlt.getGiaCho()));
        });

        colGiaGiam.setCellValueFactory(param -> new SimpleStringProperty(df.format(param.getValue().getGiaGiam())));

        colThanhTien.setCellValueFactory(param -> new SimpleStringProperty(df.format(param.getValue().getGiaVe())));

        tbCTHD.setItems(FXCollections.observableArrayList(Collections.singletonList(cthd)));

        ChiTietHoaDon cthd_old = cthd_dao.getCT_HoaDonTheoMaVe(ve_doi.getMaVe());
        hd.tinhTongGiamGia(new ArrayList<>(List.of(cthd)));
        KhuyenMai km = km_dao.getKMTheoMa(hd.getKhuyenMai().getMaKM());
        double gia = Math.round((cthd.getGiaVe() - cthd_old.getGiaVe()) / 1000) * 1000;
        tongTien = (gia > 0 ? gia * (1 - km.getMucKM()) : gia) + 10000;
        double giamGia = gia > 0 ? gia * hd.getKhuyenMai().getMucKM() : 0 + cthd.getGiaGiam();
        tongTien = Math.round(tongTien / 1000) * 1000;
        giamGia = Math.round(giamGia / 1000) * 1000;
        if (tongTien <= 0) {
            txtTienKH.setText("0");
            txtTienTra.setText(df.format(-tongTien));
            txtTienTra.setDisable(true);
            txtTienKH.setDisable(true);
        } else {
            txtThanhTien.setText(df.format(tongTien));
            txtTienTra.setDisable(false);
        }

        hd = new HoaDon(hd.getMaHoaDon(), getData.nv, getData.kh, LocalDateTime.now(), km_dao.getKMGiamCaoNhat(), tongTien, giamGia,false);

        if (!txtThanhTien.getText().isEmpty() && txtTienKH.getText() != null) {
            double t = hd.getTongTien()/1000;
            t = Math.round(t);
            btnGia1.setText(df.format(t * 1000));
            double x1 = hd.getTongTien() / 1000000;
            x1 = (int) x1;
            if (tongTien % 1000000 < 10000) {
                btnGia2.setText(df.format(x1 * 1000000 + 10000));
                btnGia3.setText(df.format(x1 * 1000000 + 20000));
                btnGia4.setText(df.format(x1 * 1000000 + 50000));
            } else if (tongTien % 1000000 < 20000) {
                btnGia2.setText(df.format(x1 * 1000000 + 20000));
                btnGia3.setText(df.format(x1 * 1000000 + 50000));
                btnGia4.setText(df.format((x1 + 1) * 100000));
            } else if (tongTien % 1000000 < 50000) {
                btnGia2.setText(df.format(x1 * 1000000 + 50000));
                btnGia3.setText(df.format(x1 * 1000000 + 100000));
                btnGia4.setText(df.format((x1 + 1) * 100000));
            } else if (tongTien % 1000000 < 100000) {
                btnGia2.setText(df.format(x1 * 1000000 + 100000));
                btnGia3.setText(df.format(x1 * 1000000 + 200000));
                btnGia4.setText(df.format(x1 * 1000000 + 500000));
            } else if (tongTien % 1000000 < 200000) {
                btnGia2.setText(df.format(x1 * 1000000 + 200000));
                btnGia3.setText(df.format(x1 * 1000000 + 500000));
                btnGia4.setText(df.format((x1 + 1) * 1000000));
            } else if (tongTien % 1000000 < 500000) {
                btnGia2.setText(df.format(x1 * 1000000 + 500000));
                btnGia3.setText(df.format(x1 * 1000000 + 700000));
                btnGia4.setText(df.format((x1 + 1) * 1000000));
            } else {
                btnGia2.setText(df.format((x1 + 1) * 1000000));
                btnGia3.setText(df.format((x1 + 1) * 1000000 + 500000));
                btnGia4.setText(df.format((x1 + 2) * 1000000));
            }
        }

        btnGia1.setOnAction(event -> {
            txtTienKH.setText(btnGia1.getText());
            double tienTra = parseCurrency(txtTienKH.getText()) - parseCurrency(txtThanhTien.getText());
            txtTienTra.setText(df.format(tienTra));
        });
        btnGia2.setOnAction(event -> {
            txtTienKH.setText(btnGia2.getText());
            double tienTra = parseCurrency(txtTienKH.getText()) - parseCurrency(txtThanhTien.getText());
            txtTienTra.setText(df.format(tienTra));
        });
        btnGia3.setOnAction(event -> {
            txtTienKH.setText(btnGia3.getText());
            double tienTra = parseCurrency(txtTienKH.getText()) - parseCurrency(txtThanhTien.getText());
            txtTienTra.setText(df.format(tienTra));
        });
        btnGia4.setOnAction(event -> {
            txtTienKH.setText(btnGia4.getText());
            double tienTra = parseCurrency(txtTienKH.getText()) - parseCurrency(txtThanhTien.getText());
            txtTienTra.setText(df.format(tienTra));
        });

        btnThanhToan.setOnAction(event -> {
            if (txtTienKH.getText().isEmpty() || txtTienKH.getText() == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText("Lỗi thanh toán");
                alert.setContentText("Vui lòng chọn số tiền khách hàng trả");
                alert.showAndWait();
            } else {
                hd.setTrangThai(true);
                getData.hd = hd;
                if(hd_dao.update(hd)) {
                    ctl_dao.updateCTLT(ve_dao.getVeTheoID(ve_doi.getMaVe()).getCtlt(), true);
                    ctl_dao.updateCTLT(ve_doi.getCtlt(), false);
                    ve_dao.update(ve_doi);
                    getData.dsve.clear();
                    getData.dsve.add(ve_doi);
                    cthd_dao.create(cthd);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Thông báo");
                    alert.setHeaderText("Thanh toán thành công");
                    btnThanhToan.setDisable(true);
                    btnInHD.setDisable(false);
                    alert.showAndWait();
                }
            }
        });

        btnInHD.setOnAction(event -> {
            PrintPDF printPDF = new PrintPDF();
            try {
                printPDF.inHoaDon(getData.hd);
                ArrayList<Ve> list = getData.dsve;
                printPDF.inVe(list);

            } catch (IOException | DocumentException e) {
                throw new RuntimeException(e);
            }
        });

        txtTienKH.setOnKeyTyped(event -> {
            goiYGia();
        });
    }

    private double parseCurrency(String text) {
        return Double.parseDouble(text.replaceAll("[^\\d]", ""));
    }

    private void goiYGia() {
        if (!txtThanhTien.getText().isEmpty() && txtTienKH.getText() != null) {
            // gợi ý giá như sau:
            // btn1: gợi ý đúng giá thành tiền
            // btn2: gợi ý giá dựa vào số được nhập trong txtTienKH * 10000
            // btn3: gợi ý giá dựa vào số được nhập trong txtTienKH * 100000
            // btn4: gợi ý giá dựa vào số được nhập trong txtTienKH * 1000000
            int tienKH = Integer.parseInt(txtTienKH.getText().replaceAll("[^\\d]", ""));
            NumberFormat df = NumberFormat.getCurrencyInstance(Locale.of("vi", "VN"));
            btnGia1.setText(df.format(Math.round(tongTien / 1000) * 1000));
            btnGia2.setText(df.format(Math.max(tienKH * 10000L, Math.ceil(tongTien / 10000) * 10000)));
            btnGia3.setText(df.format(Math.max(tienKH * 100000L, Math.ceil(tongTien / 100000) * 100000)));
            btnGia4.setText(df.format(Math.max(tienKH * 1000000L, Math.ceil(tongTien / 1000000) * 1000000)));
        }
    }
}
