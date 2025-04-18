package controller;

import com.itextpdf.text.DocumentException;
import dao.CT_HoaDon_DAO;
import dao.HoaDon_DAO;
import dao.Ve_DAO;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import entity.NhanVien;
import entity.Ve;
import gui.TrangChu_GUI;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class KetCaController implements Initializable {

    @FXML
    private Label lbl_gioMoCa;

    @FXML
    private Label lbl_tenNV;

    @FXML
    private Label lbl_tienDauCa;

    @FXML
    private Label lbl_tienTrongCa;

    @FXML
    private Label lbl_soVeBan;

    @FXML
    private Label lbl_soVeHuy;

    @FXML
    private Label lbl_soVeDoi;

    @FXML
    private Label lbl_tienBanVe;

    @FXML
    private Label lbl_tienTraVe;

    @FXML
    private Label lbl_tienTraVeDoi;

    @FXML
    private Label lbl_tienThuVeDoi;

    @FXML
    private Label lbl_tienCuoiCa;

    @FXML
    private TextField txt_tienMatThu;

    @FXML
    private TextField txt_tienChenhLech;

    @FXML
    private TextField txt_ghiChu;

    @FXML
    private Button btn_dongCa;

    @FXML
    private Button btn_inPhieu;

    @FXML
    private Button btn_BackBanVe;

    @FXML
    private Label lbl_thongBao;

    private NhanVien nv;
    private LocalDateTime gioBatDau;
    private double tienDauCa;
    private double tongTien;

    private HoaDon_DAO hoaDon_dao;
    private CT_HoaDon_DAO ct_hoaDon_dao;
    private Ve_DAO ve_dao;
    private PrintPDF printPDF;

    int soVeBanUI; // Số vé đã bán
    int soVeHuyUI; // Số vé đã hủy
    int soVeDoiUI; // Số vé đã đổi

    double tienBanVe; // Tiền bán vé
    double tienTraVe; // Tiền trả vé
    double tienTraVeDoi; // Tiền trả vé đổi
    double tienThuVeDoi; // Tiền thu vé đổi

    String ghiChu;


    NumberFormat df = NumberFormat.getCurrencyInstance(Locale.of("vi", "VN"));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initDAO();
        setInfo();

        btn_BackBanVe.setOnAction(event -> {
            Stage stage = (Stage) btn_BackBanVe.getScene().getWindow();
            stage.close();
        });

        btn_dongCa.setOnAction(event -> {
            if (!checkInput()) {
                return;
            }
            closeWindow();
        });

        btn_inPhieu.setOnAction(event -> {
            if (!checkInput()) {
                return;
            }
            if (txt_ghiChu.getText().isEmpty()) {
                ghiChu = "";
            } else {
                ghiChu = txt_ghiChu.getText();
            }
            try {
                printPDF.inKetCa(nv, gioBatDau, LocalDateTime.now(), tienDauCa, tongTien,
                                tienBanVe, tienTraVe, tienTraVeDoi, tienThuVeDoi, ghiChu);
                closeWindow();
            } catch (IOException | DocumentException e) {
                throw new RuntimeException(e);
            }
        });

        txt_tienMatThu.setOnKeyTyped(event -> {
            double tienMatThu = Double.parseDouble(txt_tienMatThu.getText());
            if (tienMatThu < 0) {
                lbl_thongBao.setText("Tiền mặt thu không được âm");
            } else {
                double tienChenhLech = tongTien - tienMatThu;
                txt_tienChenhLech.setText(df.format(tienChenhLech));
            }
        });

        // bấm esc để thoát
        txt_tienMatThu.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ESCAPE")) {
                closeWindow();
            }
        });
    }

    public void setInfo() {
        nv = getData.nv;
        gioBatDau = getData.gioMoCa;
        tienDauCa = getData.tienDauCa;

        renderDauCa();
        renderTrongCa();
        renderCuoiCa();
    }

    private void renderDauCa() {
        lbl_gioMoCa.setText(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(gioBatDau));
        lbl_tenNV.setText(nv.getTenNhanVien());
        lbl_tienDauCa.setText(df.format(Math.round(tienDauCa / 1000) * 1000));
    }

    private void renderTrongCa() {
        soVeBanUI = 0; // Số vé đã bán
        soVeHuyUI = 0; // Số vé đã hủy
        soVeDoiUI = 0; // Số vé đã đổi

        tienBanVe = 0;
        tienTraVe = 0;
        tienTraVeDoi = 0;
        tienThuVeDoi = 0;

        // Lấy các hóa đơn trong khoảng thời gian từ đầu ca đến hiện tại
        ArrayList<HoaDon> dsHoaDon = hoaDon_dao.getHoaDonTheoNV(nv.getMaNhanVien()).stream()
        .filter(hd -> hd.getNgayLapHoaDon().isAfter(gioBatDau) && hd.getNgayLapHoaDon().isBefore(LocalDateTime.now()))
        .collect(Collectors.toCollection(ArrayList::new));


        for (HoaDon hd : dsHoaDon) {
            // Get the total amount of the invoice
            double tienHoaDon = hd.getTongTien();

            // Get the list of invoice details
            ArrayList<ChiTietHoaDon> dsCTHD = ct_hoaDon_dao.getCT_HoaDon(hd.getMaHoaDon());

            // Get the list of tickets from the invoice details
            ArrayList<Ve> dsVe = dsCTHD.stream()
                    .map(cthd -> ve_dao.getVeTheoID(cthd.getVe().getMaVe()))
                    .collect(Collectors.toCollection(ArrayList::new));

            // Đếm số vé theo tình trạng
            long soVeDoi = dsVe.stream().filter(ve -> ve.getTinhTrangVe().equals("DaDoi")).count();
            long soVeHuy = dsVe.stream().filter(ve -> ve.getTinhTrangVe().equals("DaHuy")).count();
            long soVeBan = dsVe.size() - soVeDoi - soVeHuy;

            // Process the invoice based on the total amount and ticket statuses
            if (tienHoaDon > 0) {
                if (soVeBan == dsVe.size()) { // Trường hợp tất cả vé có tình trạng "DaBan"
                    tienBanVe += tienHoaDon;
                    soVeBanUI += dsVe.size();
                } else if (soVeDoi == dsVe.size()) { // Trường hợp tất cả vé có tình trạng "DaDoi"
                    tienThuVeDoi += tienHoaDon;
                    soVeDoiUI += dsVe.size();
                } else if (soVeBan > 0 && soVeHuy > 0 && soVeDoi > 0) { // Trường hợp Hóa đơn bán vé có vé "DaBan" và "DaHuy" và "DaDoi"
                    for (ChiTietHoaDon cthd : dsCTHD) {
                        Ve ve = ve_dao.getVeTheoID(cthd.getVe().getMaVe());
                        soVeBanUI++;
                        if (ve.getTinhTrangVe().equals("DaDoi") || ve.getTinhTrangVe().equals("DaHuy")) {
                            tienHoaDon -= cthd.getGiaVe() - cthd.getGiaGiam();
                            soVeBanUI--;
                        }
                    }
                    tienBanVe += tienHoaDon;
                } else if (soVeBan > 0 && (soVeHuy > 0 || soVeDoi > 0)) { // Trường hợp Hóa đơn bán vé có vé "DaHuy" hoặc "DaDoi"
                    for (ChiTietHoaDon cthd : dsCTHD) {
                        Ve ve = ve_dao.getVeTheoID(cthd.getVe().getMaVe());
                        soVeBanUI++;
                        if (ve.getTinhTrangVe().equals("DaHuy") || ve.getTinhTrangVe().equals("DaDoi")) {
                            tienHoaDon -= cthd.getGiaVe() - cthd.getGiaGiam();
                            soVeBanUI--;
                        }
                    }
                    tienBanVe += tienHoaDon;
                } else if (soVeBan == 0 && soVeDoi > 0 && soVeHuy > 0) { // Trường hợp hóa đơn đổi vé có vé "DaHuy"
                    for (ChiTietHoaDon cthd : dsCTHD) {
                        Ve ve = ve_dao.getVeTheoID(cthd.getVe().getMaVe());
                        soVeDoiUI++;
                        if (ve.getTinhTrangVe().equals("DaHuy")) {
                            tienHoaDon -= cthd.getGiaVe() - cthd.getGiaGiam();
                            soVeDoiUI--;
                        }
                    }
                    tienThuVeDoi += tienHoaDon;
                }
            } else {
                if (soVeHuy == 0) { // Trường hợp tất cả vé có tình trạng "DaDoi"
                    tienTraVeDoi += tienHoaDon;
                    soVeHuyUI += dsVe.size();
                } else if (soVeHuy == dsVe.size()) { // Trường hợp tất cả vé có tình trạng "DaHuy"
                    tienTraVe += tienHoaDon;
                    soVeHuyUI += dsVe.size();
                } else { // Trường hợp hóa đơn đổi vé có vé "DaHuy"
                    for (ChiTietHoaDon cthd : dsCTHD) {
                        Ve ve = ve_dao.getVeTheoID(cthd.getVe().getMaVe());
                        if (ve.getTinhTrangVe().equals("DaHuy")) {
                            tienHoaDon -= cthd.getGiaVe() - cthd.getGiaGiam();
                            soVeHuy--;
                        }
                    }
                    tienTraVeDoi += tienHoaDon;
                }
            }
        }

        lbl_soVeBan.setText(String.valueOf(soVeBanUI));
        lbl_soVeHuy.setText(String.valueOf(soVeHuyUI));
        lbl_soVeDoi.setText(String.valueOf(soVeDoiUI));

        tienBanVe = Math.round(tienBanVe / 1000) * 1000;
        tienTraVe = Math.round(tienTraVe / 1000) * 1000;
        tienTraVeDoi = Math.round(tienTraVeDoi / 1000) * 1000;
        tienThuVeDoi = Math.round(tienThuVeDoi / 1000) * 1000;

        tongTien = tienBanVe + tienTraVe + tienTraVeDoi + tienThuVeDoi;
        lbl_tienBanVe.setText(df.format(tienBanVe));
        lbl_tienTraVe.setText(df.format(tienTraVe));
        lbl_tienTraVeDoi.setText(df.format(tienTraVeDoi));
        lbl_tienThuVeDoi.setText(df.format(tienThuVeDoi));

        lbl_tienTrongCa.setText(df.format(tongTien));
    }

    public void renderCuoiCa() {
        tongTien += tienDauCa;
        lbl_tienCuoiCa.setText(df.format(tongTien));
        txt_tienMatThu.setPromptText(df.format(0));
        txt_tienChenhLech.setPromptText(df.format(tongTien));
    }

    private void closeWindow() {
        // Đóng cửa sổ Kết Ca
        Stage stage = (Stage) btn_dongCa.getScene().getWindow();
        stage.close();
        getData.nv = null;

        // Mở cửa sổ Mở Ca
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/mo-ca.fxml"));
            AnchorPane page = loader.load();
            Scene scene = new Scene(page);
            TrangChu_GUI.stage.close();
            TrangChu_GUI.stage.setScene(scene);
            TrangChu_GUI.stage.show();
            TrangChu_GUI.stage.centerOnScreen();
            TextField txtTienDauCa = (TextField) scene.lookup("#txt_tienDauCa");
            txtTienDauCa.setText(String.valueOf((int)tongTien));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initDAO() {
        hoaDon_dao = new HoaDon_DAO();
        ct_hoaDon_dao = new CT_HoaDon_DAO();
        ve_dao = new Ve_DAO();
        printPDF = new PrintPDF();
    }

    private boolean checkInput() {
        if (txt_tienMatThu.getText().isEmpty()) {
            lbl_thongBao.setText("Vui lòng nhập đủ thông tin");
            return false;
        }

        if (!txt_tienMatThu.getText().matches("[0-9]*")) {
            lbl_thongBao.setText("Tiền mặt thu phải là số");
            return false;
        }

        double tienChenhLech = Double.parseDouble(txt_tienChenhLech.getText().replaceAll("\\D", ""));

        if (tienChenhLech < 0) {
            lbl_thongBao.setText("Tiền mặt thu không đủ");
            return false;
        }

        return true;
    }
}
