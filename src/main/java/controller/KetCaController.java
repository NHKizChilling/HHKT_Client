package controller;

import com.itextpdf.text.DocumentException;
import entity.*;
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
import service.CT_HoaDonService;
import service.CaLamViecService;
import service.HoaDonService;
import service.VeService;

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
import java.util.List;
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

    private PrintPDF printPDF;

    private HoaDonService hoaDonService;
    private CT_HoaDonService ctHoaDonService;
    private VeService veService;
    private CaLamViecService caLamViecService;

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
        initService();
        try {
            setInfo();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

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
                LocalDateTime gioKetCa = LocalDateTime.now();

                printPDF.inKetCa(nv, gioBatDau, gioKetCa, tienDauCa, tongTien,
                                tienBanVe, tienTraVe, tienTraVeDoi, tienThuVeDoi, ghiChu);
                closeWindow();

                getData.caLamViec.setGhiChu(ghiChu);
                getData.caLamViec.setGioKetCa(gioKetCa);
                getData.caLamViec.setTienKetCa(tongTien);
                getData.caLamViec.setTrangThaiCa(false);

                caLamViecService.update(getData.caLamViec);
                getData.caLamViec = null;
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

    public void setInfo() throws RemoteException {
        nv = getData.nv;
        gioBatDau = getData.caLamViec.getGioMoCa();
        tienDauCa = getData.caLamViec.getTienDauCa();

        renderDauCa();
        renderTrongCa();
        renderCuoiCa();
    }

    private void renderDauCa() {
        lbl_gioMoCa.setText(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(gioBatDau));
        lbl_tenNV.setText(nv.getTenNV());
        lbl_tienDauCa.setText(df.format(Math.round(tienDauCa / 1000) * 1000));
    }

    private void renderTrongCa() throws RemoteException {
        soVeBanUI = 0; // Số vé đã bán
        soVeHuyUI = 0; // Số vé đã hủy
        soVeDoiUI = 0; // Số vé đã đổi

        tienBanVe = 0;
        tienTraVe = 0;
        tienTraVeDoi = 0;
        tienThuVeDoi = 0;

        // Lấy các hóa đơn trong khoảng thời gian từ đầu ca đến hiện tại
        ArrayList<HoaDon> dsHoaDon = hoaDonService.getHoaDonTheoNV(nv.getMaNV()).stream()
        .filter(hd -> hd.getNgayLapHoaDon().isAfter(gioBatDau) && hd.getNgayLapHoaDon().isBefore(LocalDateTime.now()))
        .collect(Collectors.toCollection(ArrayList::new));


        for (HoaDon hd : dsHoaDon) {
            // Get the total amount of the invoice
            double tienHoaDon = hd.getTongTien();

            // Get the list of invoice details
            List<ChiTietHoaDon> dsCTHD = ctHoaDonService.getCT_HoaDon(hd.getMaHD());

            // Get the list of tickets from the invoice details
            ArrayList<Ve> dsVe = dsCTHD.stream()
                    .map(cthd -> {
                        try {
                            return veService.getVeTheoID(cthd.getVe().getMaVe());
                        } catch (RemoteException e) {
                            throw new RuntimeException(e);
                        }
                    })
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
                        Ve ve = veService.getVeTheoID(cthd.getVe().getMaVe());
                        soVeBanUI++;
                        if (ve.getTinhTrangVe().equals("DaDoi") || ve.getTinhTrangVe().equals("DaHuy")) {
                            tienHoaDon -= cthd.getGiaVe() - cthd.getGiaGiam();
                            soVeBanUI--;
                        }
                    }
                    tienBanVe += tienHoaDon;
                } else if (soVeBan > 0 && (soVeHuy > 0 || soVeDoi > 0)) { // Trường hợp Hóa đơn bán vé có vé "DaHuy" hoặc "DaDoi"
                    for (ChiTietHoaDon cthd : dsCTHD) {
                        Ve ve = veService.getVeTheoID(cthd.getVe().getMaVe());
                        soVeBanUI++;
                        if (ve.getTinhTrangVe().equals("DaHuy") || ve.getTinhTrangVe().equals("DaDoi")) {
                            tienHoaDon -= cthd.getGiaVe() - cthd.getGiaGiam();
                            soVeBanUI--;
                        }
                    }
                    tienBanVe += tienHoaDon;
                } else if (soVeBan == 0 && soVeDoi > 0 && soVeHuy > 0) { // Trường hợp hóa đơn đổi vé có vé "DaHuy"
                    for (ChiTietHoaDon cthd : dsCTHD) {
                        Ve ve = veService.getVeTheoID(cthd.getVe().getMaVe());
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
                        Ve ve = veService.getVeTheoID(cthd.getVe().getMaVe());
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

    private void initService() {
        try {
            hoaDonService = (HoaDonService) Naming.lookup("rmi://localhost:7701/veService");
            ctHoaDonService = (CT_HoaDonService) Naming.lookup("rmi://localhost:7701/ctHoaDonService");
            veService = (VeService) Naming.lookup("rmi://localhost:7701/veService");
            caLamViecService = (CaLamViecService) Naming.lookup("rmi://localhost:7701/caLamViecService");
            printPDF = new PrintPDF();
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize services", e);
        }
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
