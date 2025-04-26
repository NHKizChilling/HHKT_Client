package controller;

import service.NhanVienService;
import entity.NhanVien;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class NhanVienController implements Initializable {

    @FXML
    private ComboBox<String> cb_search;

    @FXML
    private TextField txt_search;

    @FXML
    private Button btn_search;

    @FXML
    private TableView<NhanVien> tbl_nhanVien;

    @FXML
    private TableColumn<NhanVien, String> col_maNV;

    @FXML
    private TableColumn<NhanVien, String> col_tenNV;

    @FXML
    private TableColumn<NhanVien, String> col_gioiTinh;

    @FXML
    private TableColumn<NhanVien, String> col_dob;

    @FXML
    private TableColumn<NhanVien, String> col_cccd;

    @FXML
    private TableColumn<NhanVien, String> col_sdt;

    @FXML
    private TableColumn<NhanVien, String> col_chucVu;

    @FXML
    private TableColumn<NhanVien, String> col_tinhTrang;

    @FXML
    private TextField txt_maNV;

    @FXML
    private TextField txt_tenNV;

    @FXML
    private TextField txt_cccd;

    @FXML
    private ComboBox<String> cb_gioiTinh;

    @FXML
    private DatePicker datePicker_dob;

    @FXML
    private TextField txt_sdt;

    @FXML
    private TextField txt_email;

    @FXML
    private ComboBox<String> cb_chucVu;

    @FXML
    private ComboBox<String> cb_tinhTrangCV;

    @FXML
    private Button btn_add;

    @FXML
    private Button btn_update;

    @FXML
    private Button btn_clear;

    @FXML
    private Button btn_reset;

    private NhanVienService nhanVienService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            nhanVienService = (NhanVienService) Naming.lookup("rmi://localhost:7701/nhanVienService");
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            throw new RuntimeException(e);
        }
        ArrayList<NhanVien> list = null;
        try {
            list = (ArrayList<NhanVien>) nhanVienService.getAll();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        renderNhanVienTable(list);

        cb_gioiTinh.setItems(FXCollections.observableArrayList("Nam", "Nữ"));
        cb_chucVu.setItems(FXCollections.observableArrayList("Nhân viên", "Quản lý"));
        cb_tinhTrangCV.setItems(FXCollections.observableArrayList("Đang làm", "Đã nghỉ việc"));
        cb_search.setItems(FXCollections.observableArrayList("Mã nhân viên", "Tên nhân viên", "Số điện thoại"));

        // Chức năng tìm kiếm
        ArrayList<NhanVien> finalList = list;
        btn_search.setOnAction(e -> {
            String search = txt_search.getText().toLowerCase();
            String type = cb_search.getValue();
            if (type == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText(null);
                alert.setContentText("Chưa chọn loại tìm kiếm");
                alert.show();
                return;
            }
            if (search.isBlank()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText(null);
                alert.setContentText("Chưa nhập từ khóa tìm kiếm");
                alert.show();
                return;
            }
            ArrayList<NhanVien> listSearch = new ArrayList<>(finalList.stream()
                .filter(nv -> switch (type) {
                    case "Mã nhân viên" -> nv.getMaNV().toLowerCase().contains(search);
                    case "Tên nhân viên" -> nv.getTenNV().toLowerCase().contains(search);
                    case "Số điện thoại" -> nv.getSdt().toLowerCase().contains(search);
                    default -> false;
                })
                .toList());
            renderNhanVienTable(listSearch);
        });

        // Chức năng xóa tìm kiếm
        ArrayList<NhanVien> finalList2 = list;
        btn_reset.setOnAction(e -> {
            renderNhanVienTable(finalList2);
            txt_search.clear();
            txt_search.requestFocus();
            cb_search.getSelectionModel().clearSelection();
            btn_clear.fire();
        });

        // Chức năng hiển thị thông tin nhân viên khi click vào bảng
        tbl_nhanVien.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                NhanVien nv = tbl_nhanVien.getSelectionModel().getSelectedItem();
                txt_maNV.setText(nv.getMaNV());
                txt_tenNV.setText(nv.getTenNV());
                txt_cccd.setText(nv.getSoCCCD());
                cb_gioiTinh.setValue(nv.isGioiTinh() ? "Nữ" : "Nam");
                datePicker_dob.setValue(nv.getNgaySinh());
                txt_sdt.setText(nv.getSdt());
                txt_email.setText(nv.getEmail());
                cb_chucVu.setValue(nv.getChucVu());
                cb_tinhTrangCV.setValue(nv.getTinhTrangCv());
                btn_add.setDisable(true);
                btn_update.setDisable(false);
            }
        });

        // Chức năng xóa trắng form
        btn_clear.setOnAction(e -> {
            txt_maNV.clear();
            txt_tenNV.clear();
            txt_cccd.clear();
            cb_gioiTinh.getSelectionModel().clearSelection();
            datePicker_dob.setValue(null);
            txt_sdt.clear();
            txt_email.clear();
            cb_chucVu.getSelectionModel().clearSelection();
            cb_tinhTrangCV.getSelectionModel().clearSelection();
            btn_add.setDisable(false);
            btn_update.setDisable(true);
            tbl_nhanVien.getSelectionModel().clearSelection();
        });

        // Chức năng thêm nhân viên
        ArrayList<NhanVien> finalList1 = list;
        btn_add.setOnAction(e -> {
            String tenNV = txt_tenNV.getText();
            String cccd = txt_cccd.getText();
            boolean gioiTinh = "Nữ".equals(cb_gioiTinh.getValue());
            LocalDate dob = datePicker_dob.getValue();
            String sdt = txt_sdt.getText();
            String email;
            if (txt_email.getText().isBlank()) {
                email = null;
            } else {
                email = txt_email.getText();
            }
            String chucVu = cb_chucVu.getValue();
            String tinhTrangCV = cb_tinhTrangCV.getValue();

            // tạo mã nhân viên
            String maNVcuoi = finalList1.getLast().getMaNV();
            String maNV = "NV" + (Integer.parseInt(maNVcuoi.substring(2)) + 1);

            if (invalidInput(tenNV, cccd, dob, sdt, chucVu)) return;
            NhanVien nv = new NhanVien(maNV, tenNV, cccd, dob, gioiTinh, sdt, chucVu, email, tinhTrangCV);
            nv.setChucVu(chucVu);
            nv.setEmail(email);
            try {
                if (nhanVienService.create(nv)) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Thành công");
                    alert.setHeaderText(null);
                    alert.setContentText("Thêm nhân viên thành công");
                    alert.show();
                    finalList1.add(nv);
                    renderNhanVienTable(finalList1);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Lỗi");
                    alert.setHeaderText(null);
                    alert.setContentText("Thêm nhân viên thất bại");
                    alert.show();
                }
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        });

        // Chức năng cập nhật nhân viên
        ArrayList<NhanVien> finalList3 = list;
        btn_update.setOnAction(e -> {
            String maNV = txt_maNV.getText();
            String tenNV = txt_tenNV.getText();
            String cccd = txt_cccd.getText();
            boolean gioiTinh = "Nữ".equals(cb_gioiTinh.getValue());
            LocalDate dob = datePicker_dob.getValue();
            String sdt = txt_sdt.getText();
            String email = txt_email.getText();
            String chucVu = cb_chucVu.getValue();
            String tinhTrangCV = cb_tinhTrangCV.getValue();
            if (maNV.isBlank()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText(null);
                alert.setContentText("Chưa chọn nhân viên cần cập nhật");
                alert.show();
                return;
            }
            if (invalidInput(tenNV, cccd, dob, sdt, chucVu)) return;
            NhanVien nv = new NhanVien(maNV, tenNV, cccd, dob, gioiTinh, sdt, email, chucVu, tinhTrangCV);
            try {
                if (nhanVienService.updateInfo(nv)) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Thành công");
                    alert.setHeaderText(null);
                    alert.setContentText("Cập nhật nhân viên thành công");
                    alert.show();
                    finalList3.set(finalList3.indexOf(nv), nv);
                    renderNhanVienTable(finalList3);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Lỗi");
                    alert.setHeaderText(null);
                    alert.setContentText("Cập nhật nhân viên thất bại");
                    alert.show();
                }
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private boolean invalidInput(String tenNV, String cccd, LocalDate dob, String sdt, String chucVu) {
        if (tenNV.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Tên nhân viên không được để trống");
            alert.show();
            txt_tenNV.requestFocus();
            return true;
        }
        if (cccd.length() != 9 && cccd.length() != 12) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Số CCCD không hợp lệ");
            alert.show();
            txt_cccd.requestFocus();
            return true;
        }
        if (cccd.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Số CCCD không được để trống");
            alert.show();
            txt_cccd.requestFocus();
            return true;
        }
        if (dob == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Ngày sinh không được để trống");
            alert.show();
            datePicker_dob.requestFocus();
            return true;
        } else {
            if (dob.isAfter(LocalDate.now().minusYears(18))) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText(null);
                alert.setContentText("Ngày sinh không hợp lệ");
                alert.show();
                datePicker_dob.requestFocus();
                return true;
            }
        }
        if (sdt.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Số điện thoại không được để trống");
            alert.show();
            txt_sdt.requestFocus();
            return true;
        } else {
            if (!sdt.matches("0\\d{9}")) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText(null);
                alert.setContentText("Số điện thoại không hợp lệ");
                alert.show();
                txt_sdt.requestFocus();
                return true;
            }
        }
        if (chucVu.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng chọn chức vụ");
            alert.show();
            cb_chucVu.requestFocus();
            return true;
        }
        return false;
    }

    public void renderNhanVienTable(ArrayList<NhanVien> list) {
        ObservableList<NhanVien> listNhanVien = FXCollections.observableList(list);
        col_maNV.setCellValueFactory(new PropertyValueFactory<>("maNV"));
        col_tenNV.setCellValueFactory(new PropertyValueFactory<>("tenNV"));
        col_gioiTinh.setCellValueFactory(cellData -> {
            boolean gioiTinh = cellData.getValue().isGioiTinh();
            return new SimpleStringProperty(gioiTinh ? "Nữ" : "Nam");
        });
        col_dob.setCellValueFactory(param -> new SimpleStringProperty(DateTimeFormatter.ofPattern("dd/MM/yyyy").format(param.getValue().getNgaySinh())));
        col_cccd.setCellValueFactory(new PropertyValueFactory<>("soCCCD"));
        col_sdt.setCellValueFactory(new PropertyValueFactory<>("sdt"));
        col_chucVu.setCellValueFactory(new PropertyValueFactory<>("chucVu"));
        col_tinhTrang.setCellValueFactory(new PropertyValueFactory<>("tinhTrangCv"));
        tbl_nhanVien.setItems(listNhanVien);
    }
}
