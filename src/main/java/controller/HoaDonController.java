/*
 * @ (#) HoaDonController.java       1.0     18/10/2024
 *
 *Copyright (c) 2024 IUH. All right reserved.
 */
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/*
 * @description:
 * @author: Hiep Nguyen
 * @date:   18/10/2024
 * version: 1.0
 */
public class HoaDonController implements Initializable {
    @FXML
    private TableView<ChiTietHoaDon> tbCTHD;
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
    private AnchorPane acpTTHD;

    @FXML
    private AnchorPane acpThanhToan;

    @FXML
    private AnchorPane paneCTVe;

    @FXML
    private JFXButton btnLamMoi;

    @FXML
    private JFXButton btnHoanTat;

    @FXML
    private ComboBox<String> cbLoaiVe;

    @FXML
    private DatePicker dpNgaySinh;

    @FXML
    private Label lblTTCN;

    @FXML
    private Label lblTTCNKH;

    @FXML
    private TextField txtSoCCCD;

    @FXML
    private TextField txtTenHK;

    @FXML
    private TextField txtMaHD;
    @FXML
    private TextField txtMaNV;
    @FXML
    private TextField txtNgayLapHD;
    @FXML
    private TextField txtKH;
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
    private JFXButton btnInHD;
    @FXML
    private JFXButton btnLuuTamHD;
    @FXML
    private JFXButton btnThanhToan;
    @FXML
    private Button btnGia1;

    @FXML
    private Button btnGia2;

    @FXML
    private Button btnGia3;

    @FXML
    private Button btnGia4;

    @FXML
    private ComboBox<String> cbKM;


    private VeService veService;
    private GaService gaService;
    private CT_LichTrinhService ctltService;
    private CT_HoaDonService cthdService;
    private HoaDonService hdService;
    private ToaService toaService;
    private LoaiToaService ltoaService;
    private ChoNgoiService cnService;
    private LichTrinhService lichTrinhService;
    private KhuyenMaiService kmService;
    private LoaiVeService lvService;


    private ArrayList<Ve> dsve;
    private ArrayList<ChiTietHoaDon> dscthd;
    private double tongTien = 0;
    private List<KhuyenMai> dsKM;
    private List<ChiTietLichTrinh> dsctlt;
    private List<ChiTietLichTrinh> dsctltkh;
    private int i = 0;
    private final NumberFormat df = DecimalFormat.getCurrencyInstance(Locale.of("vi", "VN"));


    @SneakyThrows
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initService();
        try {
            dsKM = kmService.getKMHienCo();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        if (!dsKM.isEmpty()) {
            dsKM.forEach(km -> cbKM.getItems().add(km.getMoTa()));
        } else {
            cbKM.setPromptText("Không có khuyến mãi");
            cbKM.setDisable(true);
        }
        NhanVien nhanVien = getData.nv;
        HoaDon hd = getData.hd;
        KhuyenMai km = null;
        try {
            km = kmService.getKMGiamCaoNhat();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        if (km != null) {
            hd.setKhuyenMai(km);
            cbKM.setValue(km.getMoTa());
        }
        txtMaNV.setText(nhanVien.getMaNV());
        txtTenNV.setText(nhanVien.getTenNV());
        txtMaHD.setText(getData.hd.getMaHD());
        txtNgayLapHD.setText(getData.hd.getNgayLapHoaDon().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        txtKH.setText(getData.kh.getTenKH());
        txtSDT.setText(getData.kh.getSdt());
        dscthd = new ArrayList<>();
        dsve = new ArrayList<>();

        colSTT.setCellValueFactory(column -> new SimpleIntegerProperty(tbCTHD.getItems().indexOf(column.getValue()) + 1).asObject());

        colTTVe.setCellValueFactory(param -> {
            Ve ve = param.getValue().getVe();
            LichTrinh lt1 = null;
            try {
                lt1 = lichTrinhService.getLichTrinhTheoID(ve.getChiTietLichTrinh().getLichTrinh().getMaLichTrinh());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            try {
                return new SimpleStringProperty(  lt1.getSoHieuTau().getSoHieuTau()+ " - " + gaService.getGaTheoMaGa(lt1.getGaDi().getMaGa()).getTenGa() + " - " + gaService.getGaTheoMaGa(lt1.getGaDen().getMaGa()).getTenGa() + "\n" + formatter1.format(lt1.getThoiGianKhoiHanh()) + " - " + formatter1.format(lt1.getThoiGianDuKienDen()));
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });

        colLoaiCho.setCellValueFactory(param -> {
            Ve ve = param.getValue().getVe();
            ChoNgoi cn1 = null;
            try {
                cn1 = cnService.getChoNgoiTheoMa(ve.getChiTietLichTrinh().getChoNgoi().getMaCho());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            try {
                return new SimpleStringProperty(ltoaService.getLoaiToaTheoMa(toaService.getToaTheoID(cn1.getToa().getMaToa()).getLoaiToa().getMaLoaiToa()).getTenLoaiToa());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });

        colLoaiVe.setCellValueFactory(param -> {
            Ve ve = param.getValue().getVe();

            return new SimpleStringProperty(ve.getLoaiVe().getTenLoaiVe());
        });

        colGiaVe.setCellValueFactory(param -> new SimpleStringProperty(df.format(param.getValue().getVe().getChiTietLichTrinh().getGiaCho())));

        colGiaGiam.setCellValueFactory(param -> new SimpleStringProperty(df.format(param.getValue().getGiaGiam())));

        colThanhTien.setCellValueFactory(param -> new SimpleStringProperty(df.format(param.getValue().getGiaVe())));

        btnLamMoi.setOnAction(e -> {
            txtTenHK.clear();
            txtSoCCCD.clear();
            dpNgaySinh.setValue(null);
            cbLoaiVe.setValue(null);
        });
        try {
            cbLoaiVe.getItems().addAll(lvService.getAllLoaiVe().stream().map(LoaiVe::getTenLoaiVe).toList());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        //enable dpNgaySinh khi chọn loại vé Người cao tuổi hoặc Trẻ em
        cbLoaiVe.setOnAction(e -> {
            if (cbLoaiVe.getValue() != null) {
                if (cbLoaiVe.getValue().equals("Người cao tuổi") || cbLoaiVe.getValue().equals("Trẻ em")) {
                    dpNgaySinh.setDisable(false);
                    if (cbLoaiVe.getValue().equals("Trẻ em")) {
                        dpNgaySinh.setDayCellFactory(picker -> new DateCell() {
                            @Override
                            public void updateItem(LocalDate date, boolean empty) {
                                super.updateItem(date, empty);
                                setDisable(empty || date.isAfter(LocalDate.now().minusYears(6)) || date.isBefore(LocalDate.now().minusYears(14)));
                            }
                        });
                        //focus vào khoảng thời gian cho phép chọn
                        dpNgaySinh.setValue(LocalDate.now().minusYears(6));
                    } else {
                        dpNgaySinh.setDayCellFactory(picker -> new DateCell() {
                            @Override
                            public void updateItem(LocalDate date, boolean empty) {
                                super.updateItem(date, empty);
                                setDisable(empty || date.isAfter(LocalDate.now().minusYears(60)));
                            }
                        });
                        dpNgaySinh.setValue(LocalDate.now().minusYears(60));
                    }
                    txtSoCCCD.setDisable(cbLoaiVe.getValue().equals("Trẻ em"));
                    txtSoCCCD.clear();
                } else {
                    dpNgaySinh.setDisable(true);
                    dpNgaySinh.setValue(null);
                    txtSoCCCD.setDisable(false);
                }
            }
        });

        if (getData.dscthd != null && getData.dsve != null) {
            dscthd = (ArrayList<ChiTietHoaDon>) getData.dscthd;
            dsve = getData.dsve;
            tbCTHD.setItems(FXCollections.observableArrayList(dscthd));
            acpTTHD.getChildren().remove(paneCTVe);
            acpTTHD.setPrefHeight(70);
            hd.tinhTongTien(dscthd);
            hd.tinhTongGiamGia(dscthd);
            tongTien = hd.getTongTien();
            txtThanhTien.setText(df.format(Math.round(tongTien / 1000) * 1000));
            goiYGia();
            acpThanhToan.setDisable(false);
            btnGia1.setOnAction(event -> {
                txtTienKH.setText(btnGia1.getText());
                double tienKHTra = parseCurrency(txtTienKH.getText());
                double thanhTien = parseCurrency(txtThanhTien.getText());
                txtTienTra.setText(df.format(tienKHTra - thanhTien));
            });

            btnGia2.setOnAction(event -> {
                txtTienKH.setText(btnGia2.getText());
                double tienKHTra = parseCurrency(txtTienKH.getText());
                double thanhTien = parseCurrency(txtThanhTien.getText());
                txtTienTra.setText(df.format(tienKHTra - thanhTien));
            });

            btnGia3.setOnAction(event -> {
                txtTienKH.setText(btnGia3.getText());
                double tienKHTra = parseCurrency(txtTienKH.getText());
                double thanhTien = parseCurrency(txtThanhTien.getText());
                txtTienTra.setText(df.format(tienKHTra - thanhTien));
            });

            btnGia4.setOnAction(event -> {
                txtTienKH.setText(btnGia4.getText());
                double tienKHTra = parseCurrency(txtTienKH.getText());
                double thanhTien = parseCurrency(txtThanhTien.getText());
                txtTienTra.setText(df.format(tienKHTra - thanhTien));
            });
            btnThanhToan.setOnAction(event -> {
                if (txtTienKH.getText().isEmpty() || txtTienKH.getText() == null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Lỗi");
                    alert.setHeaderText("Lỗi thanh toán");
                    alert.setContentText("Vui lòng chọn số tiền khách hàng trả");
                    alert.showAndWait();
                } else {
                    hd.tinhTongTien(dscthd);
                    hd.tinhTongGiamGia(dscthd);
                    hd.setTrangThai(true);
                    getData.hd = hd;
                    try {
                        if(hdService.update(hd)) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Thông báo");
                            alert.setHeaderText("Thanh toán thành công");
                            btnInHD.setDisable(false);
                            btnThanhToan.setDisable(true);
                            btnLuuTamHD.setDisable(true);
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
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Thông báo");
                    alert.setHeaderText("In hóa đơn thành công");
                    alert.show();
                } catch (IOException | DocumentException e) {
                    throw new RuntimeException(e);
                }
            });
            return;
        }
        cbLoaiVe.setValue(cbLoaiVe.getItems().getFirst());
        dsctlt = getData.dsctlt;
        dsctltkh = getData.dsctltkh;
        dsctlt.removeAll(dsctltkh);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");

        if (dsctltkh.size() > dsctlt.size()) {
            ArrayList<ChiTietLichTrinh> temp = new ArrayList<>();
            for (int j = 0; j < dsctltkh.size(); j++) {
                if(j >= dsctlt.size()) {
                    temp.add(dsctltkh.get(j));
                }
            }
            dsctlt.addAll(temp);
            dsctltkh.removeAll(temp);
        }
        try {
            showTTVe(formatter, i);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        btnHoanTat.setOnMouseClicked(e1 -> {
            if (!checkTTVe()) {
                return;
            }
            if (tbCTHD.getSelectionModel().isEmpty()) {
                i += 1;
                if (dsctlt.size() >= i) {
                    Ve ve = null;
                    try {
                        ve = new Ve("temp" + i, getData.kh, dsctlt.get(i - 1), lvService.getLoaiVeTheoTen(cbLoaiVe.getValue()), txtTenHK.getText(), txtSoCCCD.getText(), dpNgaySinh.getValue(), "DaBan", false);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                    dsve.add(ve);
                    ChiTietHoaDon cthd = new ChiTietHoaDon(hd, ve);
                    dscthd.add(cthd);
                }
                if (dsctltkh.size() >= i) {
                    Ve vekh = null;
                    try {
                        vekh = new Ve("tempkh" + i, getData.kh, dsctltkh.get(i - 1), lvService.getLoaiVeTheoTen(cbLoaiVe.getValue()), txtTenHK.getText(), txtSoCCCD.getText(), dpNgaySinh.getValue(), "DaBan", true);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                    dsve.add(vekh);
                    ChiTietHoaDon cthdkh = new ChiTietHoaDon(hd, vekh);
                    dscthd.add(cthdkh);
                }
                if (i < dsctlt.size()) {
                    nhapLaiTTVe();
                    try {
                        showTTVe(formatter, i);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                    btnHoanTat.fireEvent(e1);
                } else {

                    if (dsve.stream().allMatch(ve -> ve.getLoaiVe().getTenLoaiVe().equals("Trẻ em"))) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Lỗi");
                        alert.setHeaderText(null);
                        alert.setContentText("Trẻ em cần có người lớn đi cùng");
                        alert.showAndWait();
                        dsve.removeLast();
                        dscthd.removeLast();
                        //xóa vé và cthd vé khứ hồi nếu có
                        if (dsctltkh.size() >= i) {
                            dsve.removeLast();
                            dscthd.removeLast();
                        }
                        i--;
                        return;
                    } else {
                        btnHoanTat.setDisable(true);
                        btnLamMoi.setDisable(true);
                        acpTTHD.getChildren().remove(paneCTVe);
                        acpTTHD.setPrefHeight(70);
                        acpThanhToan.setDisable(false);
                        hd.tinhTongTien(dscthd);
                        hd.tinhTongGiamGia(dscthd);
                        tongTien = hd.getTongTien();
                        txtThanhTien.setText(df.format(Math.round(tongTien / 1000) * 1000));
                        goiYGia();
                    }
                }
            } else {
                for(int k : tbCTHD.getSelectionModel().getSelectedIndices()) {
                    Ve ve = dsve.get(k);
                    ChiTietHoaDon cthd = dscthd.get(k);
                    try {
                        ve.setLoaiVe(lvService.getLoaiVeTheoTen(cbLoaiVe.getValue()));
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                    ve.setTenKH(txtTenHK.getText());
                    ve.setSoCCCD(txtSoCCCD.getText());
                    ve.setNgaySinh(dpNgaySinh.getValue());
                    dsve.set(k, ve);
                    cthd.setVe(ve);
                    cthd.tinhGiaVe();
                    cthd.tinhGiaGiam();
                    dscthd.set(k, cthd);
                }
                nhapLaiTTVe();
                try {
                    showTTVe(formatter, i);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                btnHoanTat.fireEvent(e1);
            }
            tbCTHD.setItems(FXCollections.observableArrayList(dscthd));
            tbCTHD.refresh();
            tbCTHD.getSelectionModel().clearSelection();
        });

        txtTienKH.setOnKeyTyped(e -> goiYGia());

        cbKM.setOnAction(e -> {
            hd.setKhuyenMai(dsKM.get(cbKM.getSelectionModel().getSelectedIndex()));
            hd.tinhTongTien(dscthd);
            hd.tinhTongGiamGia(dscthd);
            tongTien = hd.getTongTien();
            txtThanhTien.setText(df.format(Math.round(tongTien / 1000) * 1000));
            goiYGia();
        });

        btnLuuTamHD.setOnAction(event -> {
            ArrayList<HoaDon> temp_invoices = null;
            try {
                temp_invoices = hdService.getDSHDLuuTam().stream().filter(h -> !h.getNgayLapHoaDon().plusMinutes(15).isAfter(LocalDateTime.now())).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            temp_invoices = temp_invoices.stream().filter(h -> {
                try {
                    return !(cthdService.getCT_HoaDon(h.getMaHD()).isEmpty());
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            if (temp_invoices.size() >= 5) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText("Lỗi lưu tạm hóa đơn");
                alert.setContentText("Chỉ được lưu tạm 5 hóa đơn");
                alert.showAndWait();
                return;
            }
            hd.tinhTongTien(dscthd);
            hd.tinhTongGiamGia(dscthd);
            hd.setTrangThai(false);
            getData.hd = hd;
            try {
                if(hdService.update(hd)) {
                    ArrayList<Ve> list = new ArrayList<>();
                    ArrayList<ChiTietHoaDon> listcthd_new = new ArrayList<>();
                    for (Ve ve : dsve) {
                        ctltService.updateCTLT(ve.getChiTietLichTrinh(), false);
                        veService.create(ve);
                        Ve v_new = veService.getLaiVe();
                        ve.setMaVe(v_new.getMaVe());
                        list.add(ve);
                        listcthd_new.add(new ChiTietHoaDon(hd, ve));
                    }
                    for (ChiTietHoaDon cthd : listcthd_new) {
                        cthdService.create(cthd);
                    }
                    btnThanhToan.setDisable(true);
                    btnLuuTamHD.setDisable(true);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Thông báo");
                    alert.setHeaderText("Lưu tạm hóa đơn thành công");
                    alert.showAndWait();
                    getData.dsve = list;
                    getData.dscthd = listcthd_new;
                }
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });

        tbCTHD.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tbCTHD.setOnMouseClicked(e -> {
            if(paneCTVe.isVisible() && !tbCTHD.getSelectionModel().isEmpty()) {
                int s = tbCTHD.getSelectionModel().getSelectedIndex();
                Ve ve = dsve.get(s);
                cbLoaiVe.setValue(ve.getLoaiVe().getTenLoaiVe());
                if (!txtTenHK.isDisable()) {
                    txtTenHK.setText(ve.getTenKH());
                } else {
                    txtTenHK.clear();
                }
                if (!txtSoCCCD.isDisable()) {
                    txtSoCCCD.setText(ve.getSoCCCD());
                } else {
                    txtSoCCCD.clear();
                }
                if (!dpNgaySinh.isDisable()) {
                    dpNgaySinh.setValue(ve.getNgaySinh());
                } else {
                    dpNgaySinh.setValue(null);
                }
                btnHoanTat.setDisable(false);
                btnLamMoi.setDisable(false);
                int index;

                if (!ve.isKhuHoi()) {
                    index = dsctlt.indexOf(ve.getChiTietLichTrinh());
                    if (!dsctltkh.isEmpty() && index < dsctltkh.size()) {
                        tbCTHD.getSelectionModel().select(s + 1);
                    }
                } else {
                    index = dsctltkh.indexOf(ve.getChiTietLichTrinh());
                    tbCTHD.getSelectionModel().select(s - 1);
                }
                try {
                    showTTVe(formatter, index);
                } catch (RemoteException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        btnThanhToan.setOnAction(event -> {
            if (txtTienKH.getText().isEmpty() || txtTienKH.getText() == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText("Lỗi thanh toán");
                alert.setContentText("Vui lòng chọn số tiền khách hàng trả");
                alert.showAndWait();
            } else {
                hd.tinhTongTien(dscthd);
                hd.tinhTongGiamGia(dscthd);
                hd.setTrangThai(true);
                getData.hd = hd;
                try {
                    if(hdService.update(hd)) {
                        ArrayList<Ve> list = new ArrayList<>();
                        ArrayList<ChiTietHoaDon> listcthd_new = new ArrayList<>();
                        for (Ve ve : dsve) {
                            ctltService.updateCTLT(ve.getChiTietLichTrinh(), false);
                            veService.create(ve);
                            Ve v_new = veService.getLaiVe();
                            ve.setMaVe(v_new.getMaVe());
                            list.add(ve);
                            listcthd_new.add(new ChiTietHoaDon(hd, ve));
                        }
                        if (cthdService.getCT_HoaDon(hd.getMaHD()).isEmpty()) {
                            for (ChiTietHoaDon cthd : listcthd_new) {
                                cthdService.create(cthd);
                            }
                        }
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Thông báo");
                        alert.setHeaderText("Thanh toán thành công");
                        btnInHD.setDisable(false);
                        btnThanhToan.setDisable(true);
                        btnLuuTamHD.setDisable(true);
                        alert.showAndWait();
                        getData.dsve = list;
                        getData.dscthd = listcthd_new;
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
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thông báo");
                alert.setHeaderText("In hóa đơn thành công");
                alert.show();
            } catch (IOException | DocumentException e) {
                throw new RuntimeException(e);
            }
        });

        btnGia1.setOnAction(event -> {
            txtTienKH.setText(btnGia1.getText());
            double tienKHTra = parseCurrency(txtTienKH.getText());
            double thanhTien = parseCurrency(txtThanhTien.getText());
            txtTienTra.setText(df.format(tienKHTra - thanhTien));
        });

        btnGia2.setOnAction(event -> {
            txtTienKH.setText(btnGia2.getText());
            double tienKHTra = parseCurrency(txtTienKH.getText());
            double thanhTien = parseCurrency(txtThanhTien.getText());
            txtTienTra.setText(df.format(tienKHTra - thanhTien));
        });

        btnGia3.setOnAction(event -> {
            txtTienKH.setText(btnGia3.getText());
            double tienKHTra = parseCurrency(txtTienKH.getText());
            double thanhTien = parseCurrency(txtThanhTien.getText());
            txtTienTra.setText(df.format(tienKHTra - thanhTien));
        });

        btnGia4.setOnAction(event -> {
            txtTienKH.setText(btnGia4.getText());
            double tienKHTra = parseCurrency(txtTienKH.getText());
            double thanhTien = parseCurrency(txtThanhTien.getText());
            txtTienTra.setText(df.format(tienKHTra - thanhTien));
        });

        txtTienKH.setOnKeyTyped(e -> goiYGiaKhiNhap());
    }

    private double parseCurrency(String text) {
        return Double.parseDouble(text.replaceAll("[^\\d]", ""));
    }

    private void goiYGia() {
    if (!txtThanhTien.getText().isEmpty() && txtTienKH.getText() != null) {
        int t = (int) Math.round(tongTien / 1000.0);
        btnGia1.setText(df.format(t * 1000L));
        double x1 = tongTien / 1000000;
        x1 = (int) x1;
        if (tongTien % 1000000 < 100000) {
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
    }

    private void goiYGiaKhiNhap() {
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

    private void showTTVe(DateTimeFormatter formatter, int i) throws RemoteException {
        if (!dsctlt.isEmpty()) {
            ChiTietLichTrinh ctlt = dsctlt.get(i);
            LichTrinh lt = lichTrinhService.getLichTrinhTheoID(ctlt.getLichTrinh().getMaLichTrinh());
            ChoNgoi cn = cnService.getChoNgoiTheoMa(ctlt.getChoNgoi().getMaCho());
            Toa toa = toaService.getToaTheoID(cn.getToa().getMaToa());
            LoaiToa lttoa = ltoaService.getLoaiToaTheoMa(toa.getLoaiToa().getMaLoaiToa());
            lblTTCN.setText("Vé đi: " + lt.getSoHieuTau().getSoHieuTau()+ " - " + gaService.getGaTheoMaGa(lt.getGaDi().getMaGa()).getTenGa() + " - " + gaService.getGaTheoMaGa(lt.getGaDen().getMaGa()).getTenGa() + " - Chỗ " + cn.getSttCho() + " " + lttoa.getTenLoaiToa() + "\n" + formatter.format(lt.getThoiGianKhoiHanh()) + " - " + formatter.format(lt.getThoiGianDuKienDen()));
        }
        if (!dsctltkh.isEmpty() && i < dsctltkh.size()) {
            ChiTietLichTrinh ctltkh = dsctltkh.get(i);
            LichTrinh ltkh = lichTrinhService.getLichTrinhTheoID(ctltkh.getLichTrinh().getMaLichTrinh());
            ChoNgoi cnkh = cnService.getChoNgoiTheoMa(ctltkh.getChoNgoi().getMaCho());
            Toa toakh = toaService.getToaTheoID(cnkh.getToa().getMaToa());
            LoaiToa lttoakh = ltoaService.getLoaiToaTheoMa(toakh.getLoaiToa().getMaLoaiToa());
            lblTTCNKH.setText("Vé về: " + ltkh.getSoHieuTau().getSoHieuTau() + " - " + gaService.getGaTheoMaGa(ltkh.getGaDi().getMaGa()).getTenGa() + " - " + gaService.getGaTheoMaGa(ltkh.getGaDen().getMaGa()).getTenGa() + " - Chỗ " + cnkh.getSttCho() + " " + lttoakh.getTenLoaiToa() + "\n" + formatter.format(ltkh.getThoiGianKhoiHanh()) + " - " + formatter.format(ltkh.getThoiGianDuKienDen()));
        } else {
            lblTTCNKH.setText("");
        }
    }

    public boolean checkTTVe() {
        if (cbLoaiVe.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thông báo");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng chọn loại vé");
            alert.show();
            cbLoaiVe.requestFocus();
            return false;
        }
        if (txtTenHK.getText() == null || txtTenHK.getText().trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thông báo");
            alert.setHeaderText(null);
            alert.setContentText("Nhập thông tin hành khách");
            alert.show();
            txtTenHK.requestFocus();
            return false;
        }
        if (!txtSoCCCD.isDisable()) {
            if (txtSoCCCD.getText() == null || txtSoCCCD.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thông báo");
                alert.setHeaderText(null);
                alert.setContentText("Vui lòng nhập số CCCD");
                alert.show();
                txtSoCCCD.requestFocus();
                return false;
            } else {
                if (!txtSoCCCD.getText().matches("^0[0-9]{11}$")) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Thông báo");
                    alert.setHeaderText(null);
                    alert.setContentText("Số CCCD chỉ chứa 12 chữ số");
                    alert.show();
                    txtSoCCCD.requestFocus();
                    return false;
                }
            }
        }
        if (!dpNgaySinh.isDisable() && dpNgaySinh.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thông báo");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng chọn ngày sinh");
            alert.show();
            dpNgaySinh.requestFocus();
            return false;
        } else {
            if (dpNgaySinh.getValue() != null) {
                if (cbLoaiVe.getValue().equals("Trẻ em")) {
                    if (dpNgaySinh.getValue().isAfter(LocalDate.now().minusYears(6)) || dpNgaySinh.getValue().isBefore(LocalDate.now().minusYears(14))) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Lỗi");
                        alert.setHeaderText(null);
                        alert.setContentText("Trẻ em từ 6 đến 14 tuổi");
                        alert.showAndWait();
                        dpNgaySinh.requestFocus();
                        return false;
                    }
                } else {
                    if (dpNgaySinh.getValue().isAfter(LocalDate.now().minusYears(60))) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Lỗi");
                        alert.setHeaderText(null);
                        alert.setContentText("Người cao tuổi từ 60 tuổi trở lên");
                        alert.showAndWait();
                        dpNgaySinh.requestFocus();
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public void nhapLaiTTVe() {
        txtTenHK.clear();
        txtSoCCCD.clear();
        dpNgaySinh.setValue(null);
        cbLoaiVe.setValue(cbLoaiVe.getItems().getFirst());
        cbLoaiVe.setPromptText("Chọn loại vé");

        txtTenHK.requestFocus();
    }
    public void initService() throws MalformedURLException, NotBoundException, RemoteException {
        try {
            lichTrinhService = (LichTrinhService) Naming.lookup("rmi://localhost:7701/LichTrinhService");
            veService = (VeService) Naming.lookup("rmi://localhost:7701/VeService");
            gaService = (GaService) Naming.lookup("rmi://localhost:7701/GaService");
            ctltService = (CT_LichTrinhService) Naming.lookup("rmi://localhost:7701/CT_LichTrinhService");
            cthdService = (CT_HoaDonService) Naming.lookup("rmi://localhost:7701/CT_HoaDonService");
            hdService = (HoaDonService) Naming.lookup("rmi://localhost:7701/HoaDonService");
            toaService = (ToaService) Naming.lookup("rmi://localhost:7701/ToaService");
            ltoaService = (LoaiToaService) Naming.lookup("rmi://localhost:7701/LoaitoaService");
            cnService = (ChoNgoiService) Naming.lookup("rmi://localhost:7701/ChoNgoiService");
            kmService = (KhuyenMaiService) Naming.lookup("rmi://localhost:7701/KhuyenmaiService");
            lvService = (LoaiVeService) Naming.lookup("rmi://localhost:7701/LoaiVeService");
        }catch (NotBoundException | MalformedURLException | RemoteException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize services", e);
        }
    }
}

