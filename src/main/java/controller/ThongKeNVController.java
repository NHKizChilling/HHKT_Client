package controller;

import entity.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import service.*;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class ThongKeNVController implements Initializable {

    @FXML
    private ComboBox<String> cb_thongKe;

    @FXML
    private ComboBox<String> cb_thang;

    @FXML
    private ComboBox<String> cb_nam;

    @FXML
    private Button btn_search;

    @FXML
    private Label lbl_soNv;

    @FXML
    private Label lbl_taoBaoCaoNV;

    @FXML
    private BarChart<String, Number> barChart_top5nv;

    @FXML
    private Label lbl_soChuyenTau;

    @FXML
    private Label lbl_taoBaoCaoChuyenTau;

    @FXML
    private BarChart<String, Number> barChart_chuyenTau;

    private NhanVienService nhanVienService;
    private HoaDonService hoaDonService;
    private VeService veService;
    private LichTrinhService lichTrinhService;
    private GaService gaService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LocalDate currentDate = LocalDate.now();
        try {
            initDAO();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        try {
            initComponent(currentDate);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        cb_thongKe.setOnAction(event -> {
            if (cb_thongKe.getValue().equals("Tháng")) {
                cb_thang.setDisable(false);
                cb_nam.setDisable(false);
            } else {
                cb_thang.setDisable(true);
                cb_nam.setDisable(false);
            }
        });

        btn_search.setOnAction(event -> {
            if (cb_thongKe.getValue().equals("Tháng")) {
                LocalDate date = LocalDate.of(Integer.parseInt(cb_nam.getValue()), Integer.parseInt(cb_thang.getValue()), 1);
                try {
                    renderNhanVien(date, 1);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                try {
                    renderChuyenTau();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            } else {
                LocalDate date = LocalDate.of(Integer.parseInt(cb_nam.getValue()), 1, 1);
                try {
                    renderNhanVien(date, 2);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                try {
                    renderChuyenTau();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        lbl_taoBaoCaoNV.setOnMouseClicked(event -> {
            try {
                ghiFileExcelNV();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thông báo");
            alert.setHeaderText(null);
            alert.setContentText("Tạo báo cáo nhân viên thành công");
            alert.showAndWait();
        });

        lbl_taoBaoCaoChuyenTau.setOnMouseClicked(event -> {
            try {
                ghiFileExcelChuyenTau();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thông báo");
            alert.setHeaderText(null);
            alert.setContentText("Tạo báo cáo chuyến tàu thành công");
            alert.showAndWait();
        });
    }

    private void initComponent(LocalDate currentDate) throws RemoteException {
        initComboBox(currentDate);
        renderNhanVien(currentDate, 1);
        renderChuyenTau();
    }

    private void initDAO() throws MalformedURLException, NotBoundException, RemoteException {
        nhanVienService = (NhanVienService) Naming.lookup("rmi://localhost:7701/NhanVienService");
        hoaDonService = (HoaDonService) Naming.lookup("rmi://localhost:7701/HoaDonService");
        veService = (VeService) Naming.lookup("rmi://localhost:7701/VeService");
        lichTrinhService = (LichTrinhService) Naming.lookup("rmi://localhost:7701/LichTrinhService");
        gaService = (GaService) Naming.lookup("rmi://localhost:7701/GaService");
    }

    private void initComboBox(LocalDate date) {
        cb_thongKe.getItems().addAll("Tháng", "Năm");
        cb_thang.getItems().addAll("1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                "11", "12");
        cb_nam.getItems().addAll("2020", "2021", "2022", "2023", "2024");

        cb_thongKe.setValue("Tháng");
        cb_thang.setValue(String.valueOf(date.getMonthValue()));
        cb_nam.setValue(String.valueOf(date.getYear()));

        // Tự động thêm năm vào combobox
        int currentYear = LocalDate.now().getYear();
        String lastYearStr = cb_nam.getItems().isEmpty() ? "0" : cb_nam.getItems().getLast();
        int lastYear = Integer.parseInt(lastYearStr);
        for (int i = lastYear + 1; i <= currentYear; i++) {
            cb_nam.getItems().add(String.valueOf(i));
        }
    }

    private void renderNhanVien(LocalDate date, int type) throws RemoteException { //type: 1: thống kê theo tháng, 2: thống kê theo năm
        List<NhanVien> listNhanVien = nhanVienService.getDSNhanVien();
        lbl_soNv.setText(listNhanVien.size() + " nhân viên");

        List<HoaDon> listHoaDon = (type == 1)
                ? hoaDonService.getDSHDTheoThang(date.getMonthValue(), date.getYear())
                : hoaDonService.getDSHDTheoNam(String.valueOf(date.getYear()));

        // nếu ds hóa đơn rỗng null thì return
        if (listHoaDon == null || listHoaDon.isEmpty()) {
            return;
        }

        HashMap<String, Double> mapNV = new HashMap<>();
        for (HoaDon hoaDon : listHoaDon) {
            String maNV = hoaDon.getNhanVien().getMaNV();
            String tenNhanVien = nhanVienService.getNhanVien(maNV).getTenNV();
            double doanhThu = hoaDon.getTongTien();
            mapNV.put(tenNhanVien, mapNV.getOrDefault(tenNhanVien, 0.0) + doanhThu);
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        mapNV.entrySet().stream()
                .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                .sorted((k1, k2) -> -k1.getValue().compareTo(k2.getValue()))
                .limit(5)
                .forEach(entry -> series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));

        barChart_top5nv.getData().clear();
        barChart_top5nv.getData().add(series);
        barChart_top5nv.setTitle("Top 5 nhân viên có doanh thu cao nhất");
        barChart_top5nv.getXAxis().setLabel("Nhân viên");
        barChart_top5nv.getYAxis().setLabel("Doanh thu");
    }

    private void renderChuyenTau() throws RemoteException { //type: 1: thống kê theo tháng, 2: thống kê theo năm
        List<LichTrinh> listLichTrinh = lichTrinhService.getDSLichTrinhTheoTrangThai(true);
        int soChuyenTau = listLichTrinh.size();
        lbl_soChuyenTau.setText(soChuyenTau + " chuyến tàu");


        ArrayList<Ve> dsVe = new ArrayList<>(veService.getVeTheoTinhTrang("DaBan"));
        dsVe.addAll(veService.getVeTheoTinhTrang("DaDoi"));

        HashMap<String, Integer> mapChuyenTau = new HashMap<>();

        for (Ve ve : dsVe) {
            String maLichTrinh = ve.getChiTietLichTrinh().getLichTrinh().getMaLichTrinh();
            LichTrinh lichTrinh = lichTrinhService.getLichTrinhTheoID(maLichTrinh);
            Ga diemDen = gaService.getGaTheoMaGa(lichTrinh.getGaDen().getMaGa());
            String tenGaDen = diemDen.getTenGa();
            mapChuyenTau.put(tenGaDen, mapChuyenTau.getOrDefault(tenGaDen, 0) + 1);
        }

        // Hiển thị số chuyến tàu lên biểu đồ BarChart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        mapChuyenTau.forEach((key, value) -> {
            if (key != null && value != null) {
                series.getData().add(new XYChart.Data<>(key, value));
            }
        });

        barChart_chuyenTau.getData().clear();
        barChart_chuyenTau.getData().add(series);
        barChart_chuyenTau.setTitle("Số chuyến tàu đến các ga");
        barChart_chuyenTau.getXAxis().setLabel("Điểm đến");
        barChart_chuyenTau.getYAxis().setLabel("Xu hướng mua vé");
    }

    private void ghiFileExcelNV() throws RemoteException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Thống kê nhân viên");

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);

        CellStyle style1 = workbook.createCellStyle();
        style1.setBorderRight(BorderStyle.THIN);
        style1.setBorderLeft(BorderStyle.THIN);
        style1.setBorderBottom(BorderStyle.THIN);
        style1.setBorderTop(BorderStyle.THIN);

        CellStyle style2 = workbook.createCellStyle();
        style2.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style2.setBorderBottom(BorderStyle.THIN);
        style2.setBorderTop(BorderStyle.THIN);
        style2.setBorderRight(BorderStyle.THIN);
        style2.setBorderLeft(BorderStyle.THIN);
//
//        CellStyle style3 = workbook.createCellStyle();
//        style3.setBorderRight(BorderStyle.THIN);
//        style3.setBorderLeft(BorderStyle.THIN);
//        style3.setBorderTop(BorderStyle.THIN);
//
//        CellStyle style4 = workbook.createCellStyle();
//        style4.setBorderRight(BorderStyle.THIN);
//        style4.setBorderLeft(BorderStyle.THIN);
//        style4.setBorderBottom(BorderStyle.THIN);
//
//        CellStyle style5 = workbook.createCellStyle();
//        style5.setBorderRight(BorderStyle.THIN);
//        style5.setBorderLeft(BorderStyle.THIN);
//        style5.setBorderBottom(BorderStyle.THIN);
//        style5.setBorderTop(BorderStyle.THIN);
// Chỉnh độ rộng cột
        sheet.setColumnWidth(0, 2000);
        sheet.setColumnWidth(1, 4000);
        sheet.setColumnWidth(2, 4000);
        sheet.setColumnWidth(3, 4000);
        sheet.setColumnWidth(4, 4000);
        sheet.setColumnWidth(5, 4000);
        sheet.setColumnWidth(6, 4000);
        sheet.setColumnWidth(7, 4000);


// Tạo header row
        String[] headers = {"STT", "Mã nhân viên", "Họ tên nhân viên", "Số điện thoại", "Email", "Ngày sinh", "Chức vụ", "Tổng doanh thu"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        List<NhanVien> listNhanVien = nhanVienService.getDSNhanVien();
        List<HoaDon> listHoaDon = hoaDonService.getAllHoaDon();
        HashMap<String, Double> mapNV = new HashMap<>();
        for (HoaDon hoaDon : listHoaDon) {
            if (hoaDon.getNhanVien() != null) {
                String tenNhanVien = hoaDon.getNhanVien().getTenNV();
                double doanhThu = hoaDon.getTongTien();
                mapNV.put(tenNhanVien, mapNV.getOrDefault(tenNhanVien, 0.0) + doanhThu);
            }
            else {
                System.out.println("Nhan vien null");
            }
        }
        int rowNum = 1;
        for (NhanVien nv : listNhanVien) {
            Row row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(0);
            cell.setCellValue(rowNum - 1);
            cell.setCellStyle(style1);

            cell = row.createCell(1);
            cell.setCellValue(nv.getMaNV());
            cell.setCellStyle(style1);

            cell = row.createCell(2);
            cell.setCellValue(nv.getTenNV());
            cell.setCellStyle(style1);

            cell = row.createCell(3);
            cell.setCellValue(nv.getSdt());
            cell.setCellStyle(style1);

            cell = row.createCell(4);
            cell.setCellValue(nv.getEmail());
            cell.setCellStyle(style1);

            cell = row.createCell(5);
            cell.setCellValue(nv.getNgaySinh().toString());
            cell.setCellStyle(style1);

            cell = row.createCell(6);
            cell.setCellValue(nv.getChucVu());
            cell.setCellStyle(style1);

            cell = row.createCell(7);
            cell.setCellValue(mapNV.getOrDefault(nv.getTenNV(), 0.0));
            cell.setCellStyle(style1);

        }

        try {
            FileOutputStream fileOut = new FileOutputStream("ThongKeNhanVien.xlsx");
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            File file = new File("ThongKeNhanVien.xlsx");
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ghiFileExcelChuyenTau() throws RemoteException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Thống kê chuyến tàu");

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);

        CellStyle style1 = workbook.createCellStyle();
        style1.setBorderRight(BorderStyle.THIN);
        style1.setBorderLeft(BorderStyle.THIN);
        style1.setBorderBottom(BorderStyle.THIN);
        style1.setBorderTop(BorderStyle.THIN);

        CellStyle style2 = workbook.createCellStyle();
        style2.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style2.setBorderBottom(BorderStyle.THIN);
        style2.setBorderTop(BorderStyle.THIN);
        style2.setBorderRight(BorderStyle.THIN);
        style2.setBorderLeft(BorderStyle.THIN);
//
//        CellStyle style3 = workbook.createCellStyle();
//        style3.setBorderRight(BorderStyle.THIN);
//        style3.setBorderLeft(BorderStyle.THIN);
//        style3.setBorderTop(BorderStyle.THIN);
//
//        CellStyle style4 = workbook.createCellStyle();
//        style4.setBorderRight(BorderStyle.THIN);
//        style4.setBorderLeft(BorderStyle.THIN);
//        style4.setBorderBottom(BorderStyle.THIN);
//
//        CellStyle style5 = workbook.createCellStyle();
//        style5.setBorderRight(BorderStyle.THIN);
//        style5.setBorderLeft(BorderStyle.THIN);
//        style5.setBorderBottom(BorderStyle.THIN);
//        style5.setBorderTop(BorderStyle.THIN);
// Chỉnh độ rộng cột
        sheet.setColumnWidth(0, 2000);
        sheet.setColumnWidth(1, 4000);
        sheet.setColumnWidth(2, 4000);
        sheet.setColumnWidth(3, 4000);
        sheet.setColumnWidth(4, 4000);
        sheet.setColumnWidth(5, 4000);
        sheet.setColumnWidth(6, 4000);

// Tạo header row
        String[] headers = {"STT", "Số hiệu tàu", "Ga đi", "Ga đến", "Ngày khởi hành", "Ngày đến", "Số vé đã bán"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        List<LichTrinh> listLichTrinh = lichTrinhService.getDSLichTrinhTheoTrangThai(true);
        ArrayList<Ve> dsVe = new ArrayList<>(veService.getVeTheoTinhTrang("DaBan"));
        dsVe.addAll(veService.getVeTheoTinhTrang("DaDoi"));
        HashMap<String, Integer> mapChuyenTau = new HashMap<>();

        for (Ve ve : dsVe) {
            String maLichTrinh = ve.getChiTietLichTrinh().getLichTrinh().getMaLichTrinh();
            mapChuyenTau.put(maLichTrinh, mapChuyenTau.getOrDefault(maLichTrinh, 0) + 1);
        }
        int rowNum = 1;
        for (LichTrinh lt : listLichTrinh) {
            Row row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(0);
            cell.setCellValue(rowNum - 1);
            cell.setCellStyle(style1);

            cell = row.createCell(1);
            cell.setCellValue(lt.getSoHieuTau().getSoHieuTau());
            cell.setCellStyle(style1);

            cell = row.createCell(2);
            cell.setCellValue(gaService.getGaTheoMaGa(lt.getGaDi().getMaGa()).getTenGa());
            cell.setCellStyle(style1);

            cell = row.createCell(3);
            cell.setCellValue(gaService.getGaTheoMaGa(lt.getGaDen().getMaGa()).getTenGa());
            cell.setCellStyle(style1);

            cell = row.createCell(4);
            cell.setCellValue(lt.getThoiGianKhoiHanh().toString());
            cell.setCellStyle(style1);

            cell = row.createCell(5);
            cell.setCellValue(lt.getThoiGianDuKienDen().toString());
            cell.setCellStyle(style1);

            cell = row.createCell(6);
            cell.setCellValue(mapChuyenTau.getOrDefault(lt.getMaLichTrinh(), 0));
            cell.setCellStyle(style1);
        }

        try {
            FileOutputStream fileOut = new FileOutputStream("ThongKeChuyenTau.xlsx");
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            File file = new File("ThongKeChuyenTau.xlsx");
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
