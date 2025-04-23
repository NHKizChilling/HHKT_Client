package controller;

import com.itextpdf.text.DocumentException;
import com.jfoenix.controls.JFXButton;
import entity.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import service.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
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

    private double tongTien = 0;
    private HoaDon hd;

    private CT_HoaDonService ctHoaDonService;
    private HoaDonService hoaDonService;
    private KhuyenMaiService khuyenMaiService;
    private CT_LichTrinhService ctLichTrinhService;
    private VeService veService;
    private LichTrinhService lichTrinhService;
    private GaService gaService;
    private ChoNgoiService choNgoiService;
    private ToaService toaService;
    private LoaiVeService loaiVeService;
    private LoaiToaService loaiToaService;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // TODO
        initService();

        NumberFormat df = DecimalFormat.getCurrencyInstance(Locale.of("vi", "VN"));
        NhanVien nhanVien = getData.nv;
        txtMaNV.setText(nhanVien.getMaNV());
        txtTenNV.setText(nhanVien.getTenNV());
        txtMaHD.setText(getData.hd.getMaHD());
        txtNgayLapHD.setText(getData.hd.getNgayLapHoaDon().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        txtKH.setText(getData.kh.getTenKH());
        txtSDT.setText(getData.kh.getSdt());
        hd = getData.hd;
        try {
            hd.setKhuyenMai(khuyenMaiService.getKMGiamCaoNhat());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        Ve ve_doi = getData.dsve.getFirst();
        ChiTietHoaDon cthd = new ChiTietHoaDon(hd, ve_doi);

        txtTienTra.setOnKeyReleased(event -> {
            goiYGia();
        });

        colSTT.setCellValueFactory(column -> new SimpleIntegerProperty(tbCTHD.getItems().indexOf(column.getValue()) + 1).asObject());

        colTTVe.setCellValueFactory(param -> {
            LichTrinh lt1 = null;
            try {
                lt1 = lichTrinhService.getLichTrinhTheoID(ve_doi.getChiTietLichTrinh().getLichTrinh().getMaLichTrinh());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            try {
                return new SimpleStringProperty(  lt1.getSoHieuTau().getSoHieuTau()+ " - "
                        + gaService.getGaTheoMaGa(lt1.getGaDi().getMaGa()).getTenGa() + " - "
                        + gaService.getGaTheoMaGa(lt1.getGaDen().getMaGa()).getTenGa() + "\n"
                        + formatter1.format(lt1.getThoiGianKhoiHanh()) + " - " + formatter1.format(lt1.getThoiGianDuKienDen()));
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });

        colLoaiCho.setCellValueFactory(param -> {
            ChoNgoi cn1 = null;
            try {
                cn1 = choNgoiService.getChoNgoiTheoMa(ve_doi.getChiTietLichTrinh().getChoNgoi().getMaCho());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            try {
                return new SimpleStringProperty(loaiToaService.getLoaiToaTheoMa(toaService.getToaTheoID(cn1.getToa().getMaToa()).getLoaiToa().getMaLoaiToa()).getTenLoaiToa());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });

        colLoaiVe.setCellValueFactory(param -> {
            try {
                return new SimpleStringProperty(loaiVeService.getLoaiVeTheoMa(ve_doi.getLoaiVe().getMaLoaiVe()).getTenLoaiVe());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });

        colGiaVe.setCellValueFactory(param -> {
            ChiTietLichTrinh ctlt = null;
            try {
                ctlt = ctLichTrinhService.getCTLTTheoCN(ve_doi.getChiTietLichTrinh().getLichTrinh().getMaLichTrinh(),
                        ve_doi.getChiTietLichTrinh().getChoNgoi().getMaCho());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            return new SimpleStringProperty(df.format(ctlt.getGiaCho()));
        });

        colGiaGiam.setCellValueFactory(param -> new SimpleStringProperty(df.format(param.getValue().getGiaGiam())));

        colThanhTien.setCellValueFactory(param -> new SimpleStringProperty(df.format(param.getValue().getGiaVe())));

        tbCTHD.setItems(FXCollections.observableArrayList(Collections.singletonList(cthd)));

        ChiTietHoaDon cthd_old = null;
        try {
            cthd_old = ctHoaDonService.getCT_HoaDonTheoMaVe(ve_doi.getMaVe());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        hd.tinhTongGiamGia(new ArrayList<>(List.of(cthd)));
        KhuyenMai km = null;
        try {
            km = khuyenMaiService.getKMTheoMa(hd.getKhuyenMai().getMaKM());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
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

        try {
            hd = new HoaDon(hd.getMaHD(), getData.nv, getData.kh, LocalDateTime.now(), khuyenMaiService.getKMGiamCaoNhat(), tongTien, giamGia,false);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

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
                try {
                    if(hoaDonService.update(hd)) {
                        ctLichTrinhService.updateCTLT(veService.getVeTheoID(ve_doi.getMaVe()).getChiTietLichTrinh(), true);
                        ctLichTrinhService.updateCTLT(ve_doi.getChiTietLichTrinh(), false);
                        veService.update(ve_doi);
                        getData.dsve.clear();
                        getData.dsve.add(ve_doi);
                        ctHoaDonService.create(cthd);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Thông báo");
                        alert.setHeaderText("Thanh toán thành công");
                        btnThanhToan.setDisable(true);
                        btnInHD.setDisable(false);
                        alert.showAndWait();
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
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

    private void initService() {
        try {
            ctHoaDonService = (CT_HoaDonService) Naming.lookup("rmi://localhost:7701/veService");
            hoaDonService = (HoaDonService) Naming.lookup("rmi://localhost:7701/hoaDonService");
            khuyenMaiService = (KhuyenMaiService) Naming.lookup("rmi://localhost:7701/khuyenMaiService");
            ctLichTrinhService = (CT_LichTrinhService) Naming.lookup("rmi://localhost:7701/ctLichTrinhService");
            veService = (VeService) Naming.lookup("rmi://localhost:7701/veService");
            lichTrinhService = (LichTrinhService) Naming.lookup("rmi://localhost:7701/lichTrinhService");
            gaService = (GaService) Naming.lookup("rmi://localhost:7701/gaService");
            choNgoiService = (ChoNgoiService) Naming.lookup("rmi://localhost:7701/choNgoiService");
            toaService = (ToaService) Naming.lookup("rmi://localhost:7701/toaService");
            loaiVeService = (LoaiVeService) Naming.lookup("rmi://localhost:7701/loaiVeService");
            loaiToaService = (LoaiToaService) Naming.lookup("rmi://localhost:7701/loaiToaService");
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize services", e);
        }
    }
}
