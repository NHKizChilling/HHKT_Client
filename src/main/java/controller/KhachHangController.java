package controller;

import com.itextpdf.text.DocumentException;
import service.*;
import entity.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import util.VeMapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class KhachHangController implements Initializable {

    @FXML
    private ComboBox<String> cb_search;

    @FXML
    private TextField txt_searchBar;

    @FXML
    private Button btn_search;

    @FXML
    private Button btn_refresh;

    @FXML
    private TableView<KhachHang> tbl_hanhKhach;

    @FXML
    private TableView<Ve> tbl_thongTinVe;


    @FXML
    private TableColumn<KhachHang, String> col_cusId;

    @FXML
    private TableColumn<KhachHang, String> col_name;

    @FXML
    private TableColumn<KhachHang, String> col_cccd;

    @FXML
    private TableColumn<KhachHang, String> col_sdt;

    @FXML
    private TableColumn<KhachHang, String> col_email;

    @FXML
    private TableColumn<Ve, String> col_maVe;

    @FXML
    private TableColumn<Ve, String> col_maKH;

    @FXML
    private TableColumn<Ve, String> col_tinhTrangVe;

    @FXML
    private TableColumn<Ve, String> col_loaiVe;

    @FXML
    private TableColumn<Ve, String> col_loaiCho;

    @FXML
    private TableColumn<Ve, String> col_thongTinVe;

    @FXML
    private TableColumn<Ve, String> col_tenHK;

    @FXML
    private TextField txt_maHK;

    @FXML
    private TextField txt_tenHK;

    @FXML
    private TextField txt_cccd;

    @FXML
    private TextField txt_sdt;

    @FXML
    private TextField txt_email;

    @FXML
    private Button btn_add;

    @FXML
    private Button btn_update;

    @FXML
    private Button btn_clear;

    @FXML
    private Button btn_inVe;

    @FXML
    private AnchorPane acpFeature;

    private VeService veService;
    private KhachHangService khachHangService;
    private ChoNgoiService choNgoiService;
    private LichTrinhService lichTrinhService;
    private GaService gaService;
    private LoaiToaService loaiToaService;
    private ToaService toaService;
    private CT_HoaDonService ctHoaDonService;
    private LoaiVeService loaiVeService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initService();

        cb_search.getItems().addAll("Mã hành khách", "Số CCCD", "Số điện thoại");

        btn_search.setOnAction(e -> {
            String searchType = cb_search.getValue();
            if (searchType == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText(null);
                alert.setContentText("Vui lòng chọn loại tìm kiếm");
                alert.show();
                cb_search.requestFocus();
                return;
            }
            String searchBar = txt_searchBar.getText();
            if (searchBar.isBlank()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText(null);
                alert.setContentText("Vui lòng nhập từ khóa tìm kiếm");
                alert.show();
                txt_searchBar.requestFocus();
                return;
            }
            ArrayList<KhachHang> dsKhachHangs = new ArrayList<>();
            KhachHang hk;
            switch (searchType) {
                case "Mã hành khách":
                    try {
                        hk = khachHangService.getKhachHangTheoMaKH(searchBar);
                    } catch (RemoteException ex) {
                        throw new RuntimeException(ex);
                    }
                    if (hk != null) {
                        dsKhachHangs.add(hk);
                    }
                    break;
                case "Số CCCD":
                    try {
                        hk = khachHangService.getKHTheoCCCD(searchBar);
                    } catch (RemoteException ex) {
                        throw new RuntimeException(ex);
                    }
                    if (hk != null) {
                        dsKhachHangs.add(hk);
                    }
                    break;
                case "Số điện thoại":
                    try {
                        hk = khachHangService.getKhachHangTheoSDT(searchBar);
                    } catch (RemoteException ex) {
                        throw new RuntimeException(ex);
                    }
                    if (hk != null) {
                        dsKhachHangs.add(hk);
                    }
                    break;
            }
            if (dsKhachHangs.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thông báo");
                alert.setHeaderText(null);
                alert.setContentText("Không tìm thấy kết quả");
                alert.show();
                txt_searchBar.requestFocus();
                return;
            }
            renderTableHanhKhach(dsKhachHangs);
        });

        btn_refresh.setOnAction(e -> {
            txt_searchBar.clear();
            txt_searchBar.requestFocus();
            tbl_hanhKhach.setItems(null);
            cb_search.getSelectionModel().clearSelection();
            tbl_thongTinVe.setItems(null);
            btn_clear.fire();
        });

        tbl_hanhKhach.setOnMouseClicked(e -> {
            KhachHang hk = tbl_hanhKhach.getSelectionModel().getSelectedItem();
            txt_maHK.setText(hk.getMaKH());
            txt_tenHK.setText(hk.getTenKH());
            txt_cccd.setText(hk.getSoCCCD());
            txt_sdt.setText(hk.getSdt());
            txt_email.setText(hk.getEmail());
            btn_add.setDisable(true);
            btn_update.setDisable(false);
            ArrayList<Ve> dsVeTheoMaHK = null;
            try {
                dsVeTheoMaHK = (ArrayList<Ve>) VeMapper.toEntityList(veService.getDSVeTheoMaKH(hk.getMaKH()));
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
            renderTableVe(dsVeTheoMaHK);
        });

        btn_clear.setOnAction(e -> {
            txt_maHK.clear();
            txt_tenHK.clear();
            txt_cccd.clear();
            txt_sdt.clear();
            txt_email.clear();
            btn_add.setDisable(false);
            btn_update.setDisable(true);
            tbl_hanhKhach.getSelectionModel().clearSelection();
            tbl_thongTinVe.setItems(null);
        });

        btn_add.setOnAction(e -> {
            String tenHK = txt_tenHK.getText();
            String cccd = txt_cccd.getText();
            String sdt = txt_sdt.getText();
            String email = txt_email.getText();

            if (checkInput(tenHK, cccd, sdt, email)) {
                KhachHang hk = new KhachHang(tenHK, cccd, sdt, email);
                try {
                    if (khachHangService.create(hk)) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Thành công");
                        alert.setHeaderText("Thêm hành khách thành công");
                        alert.show();
                        btn_clear.fire();
                        renderTableHanhKhach((ArrayList<KhachHang>) khachHangService.getAllKhachHang());
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Lỗi");
                        alert.setHeaderText("Thêm hành khách thất bại");
                        alert.show();
                    }
                } catch (RemoteException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        btn_update.setOnAction(e -> {
            String maHK = txt_maHK.getText();
            String tenHK = txt_tenHK.getText();
            String cccd = txt_cccd.getText();
            String sdt = txt_sdt.getText();
            String email = txt_email.getText();

            if (maHK.isBlank()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText("Vui lòng chọn hành khách cần cập nhật");
                alert.show();
                return;
            }

            if (checkInput(tenHK, cccd, sdt, email)) {
                KhachHang hk = new KhachHang(maHK, tenHK, cccd, sdt, email);
                try {
                    if (khachHangService.update(hk)) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Thành công");
                        alert.setHeaderText("Cập nhật hành khách thành công");
                        alert.show();
                        btn_clear.fire();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Lỗi");
                        alert.setHeaderText("Cập nhật hành khách thất bại");
                        alert.show();
                    }
                } catch (RemoteException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        if (getData.nv.getChucVu().equals("Nhân viên")) {
            acpFeature.setDisable(true);
        }

        tbl_thongTinVe.setOnMouseClicked(e -> {
            Ve ve = tbl_thongTinVe.getSelectionModel().getSelectedItem();
            btn_inVe.setDisable(ve.getTinhTrangVe().equals("DaHuy"));
        });

        btn_inVe.setOnAction(e -> {
            KhachHang hk = tbl_hanhKhach.getSelectionModel().getSelectedItem();
            if (hk == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText("Vui lòng chọn hành khách cần in vé");
                alert.show();
                return;
            }
            Ve ve = tbl_thongTinVe.getSelectionModel().getSelectedItem();
            ChiTietHoaDon cthd = null;
            try {
                cthd = ctHoaDonService.getCT_HoaDonTheoMaVe(ve.getMaVe());
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
            getData.hd = new HoaDon(cthd.getHoaDon().getMaHD());
            if (ve == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText("Vui lòng chọn vé cần in");
                alert.show();
                return;
            }
            try {
                new PrintPDF().inVe(new ArrayList<>(List.of(ve)));
            } catch (IOException | DocumentException ex) {
                throw new RuntimeException(ex);
            }
        });

        // bấm enter để tìm kiếm
        txt_searchBar.setOnAction(e -> btn_search.fire());
    }

    public void renderTableHanhKhach(ArrayList<KhachHang> list) {
        ObservableList<KhachHang> dskh = FXCollections.observableArrayList(list);
        tbl_hanhKhach.setItems(dskh);
        tbl_hanhKhach.refresh();
        col_cusId.setCellValueFactory(new PropertyValueFactory<>("maKH"));
        col_name.setCellValueFactory(new PropertyValueFactory<>("tenKH"));
        col_cccd.setCellValueFactory(new PropertyValueFactory<>("soCCCD"));
        col_sdt.setCellValueFactory(new PropertyValueFactory<>("sdt"));
        col_email.setCellValueFactory(new PropertyValueFactory<>("email"));
    }

    public void renderTableVe(ArrayList<Ve> list) {
        ObservableList<Ve> data = FXCollections.observableArrayList(list);
        tbl_thongTinVe.setItems(data);
        tbl_thongTinVe.refresh();
        col_maVe.setCellValueFactory(new PropertyValueFactory<>("maVe"));
        col_maKH.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getKhachHang().getMaKH()));
        col_thongTinVe.setCellValueFactory(p -> {
            Ve ve = null;
            try {
                ve = VeMapper.toEntity(veService.getVeTheoID(p.getValue().getMaVe()));
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            LichTrinh lt = null;
            try {
                lt = lichTrinhService.getLichTrinhTheoID(ve.getChiTietLichTrinh().getLichTrinh().getMaLichTrinh());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            try {
                return new SimpleStringProperty(lt.getSoHieuTau().getSoHieuTau() + " - " + gaService.getGaTheoMaGa(lt.getGaDi().getMaGa()).getTenGa() + " - " + gaService.getGaTheoMaGa(lt.getGaDen().getMaGa()).getTenGa() + "\n" + formatter.format(lt.getThoiGianKhoiHanh()) + " - " + formatter.format(lt.getThoiGianDuKienDen()));
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
        col_loaiCho.setCellValueFactory(p -> {
            Ve ve = null;
            try {
                ve = VeMapper.toEntity(veService.getVeTheoID(p.getValue().getMaVe()));
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            ChoNgoi cn = null;
            try {
                cn = choNgoiService.getChoNgoiTheoMa(ve.getChiTietLichTrinh().getChoNgoi().getMaCho());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            try {
                return new SimpleStringProperty(loaiToaService.getLoaiToaTheoMa(toaService.getToaTheoID(cn.getToa().getMaToa()).getLoaiToa().getMaLoaiToa()).getTenLoaiToa());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });

        HashMap<String, String> tinhTrang = new HashMap<>();
        tinhTrang.put("DaHuy", "Đã hủy");
        tinhTrang.put("DaBan", "Đã bán");
        tinhTrang.put("DaDoi", "Đã đổi");

        col_tinhTrangVe.setCellValueFactory(p -> new SimpleStringProperty(tinhTrang.get(p.getValue().getTinhTrangVe())));

        col_loaiVe.setCellValueFactory(p -> {
            Ve ve = null;
            try {
                ve = VeMapper.toEntity(veService.getVeTheoID(p.getValue().getMaVe()));
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            try {
                return new SimpleStringProperty(loaiVeService.getLoaiVeTheoMa(ve.getLoaiVe().getMaLoaiVe()).getTenLoaiVe());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
        col_tenHK.setCellValueFactory(new PropertyValueFactory<>("tenHanhKhach"));
    }

    public boolean checkInput(String tenHK, String cccd, String sdt, String email) {
        if (tenHK.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Tên hành khách không được để trống");
            alert.show();
            txt_tenHK.requestFocus();
            return false;
        }
        if (cccd.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Số CCCD không được để trống");
            alert.show();
            txt_cccd.requestFocus();
            return false;
        } else if (cccd.length() != 9 && cccd.length() != 12) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Số CCCD không hợp lệ");
            alert.show();
            txt_cccd.requestFocus();
            return false;
        }

        if (sdt.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Số điện thoại không được để trống");
            alert.show();
            txt_sdt.requestFocus();
            return false;
        } else if (sdt.length() != 10) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Số điện thoại không hợp lệ");
            alert.show();
            txt_sdt.requestFocus();
            return false;
        }

        if (email.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Email không được để trống");
            alert.show();
            txt_email.requestFocus();
            return false;
        } else if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Email không hợp lệ");
            alert.show();
            txt_email.requestFocus();
            return false;
        }
        return true;
    }

    private void initService() {
        try {
            veService = (VeService) Naming.lookup("rmi://localhost:7701/veService");
            khachHangService = (KhachHangService) Naming.lookup("rmi://localhost:7701/khachHangService");
            choNgoiService = (ChoNgoiService) Naming.lookup("rmi://localhost:7701/choNgoiService");
            lichTrinhService = (LichTrinhService) Naming.lookup("rmi://localhost:7701/lichTrinhService");
            gaService = (GaService) Naming.lookup("rmi://localhost:7701/gaService");
            loaiToaService = (LoaiToaService) Naming.lookup("rmi://localhost:7701/loaiToaService");
            toaService = (ToaService) Naming.lookup("rmi://localhost:7701/toaService");
            ctHoaDonService = (CT_HoaDonService) Naming.lookup("rmi://localhost:7701/ctHoaDonService");
            loaiVeService = (LoaiVeService) Naming.lookup("rmi://localhost:7701/loaiVeService");
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize services", e);
        }
    }
}
