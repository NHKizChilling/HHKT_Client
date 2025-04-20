/*
 * @ (#) GioiThieuController.java       1.0     20/10/2024
 *
 *Copyright (c) 2024 IUH. All right reserved.
 */
package controller;

import entity.ChiTietHoaDon;
import entity.HoaDon;
import entity.LichTrinh;
import entity.Ve;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import lombok.SneakyThrows;
import service.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/*
 * @description:
 * @author: Hiep Nguyen
 * @date:   20/10/2024
 * version: 1.0
 */
public class GioiThieuController implements Initializable {
    @FXML
    private BarChart<String, Integer> chartChuyenTau;

    @FXML
    private Label lblDoanhThu;

    @FXML
    private Label lblSLChuyenTau;

    @FXML
    private Label lblSLVe;

    VeService ve_dao;
    CT_HoaDonService cthd_dao;
    HoaDonService hd_dao;
    LichTrinhService lichTrinh_dao;

    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initDao();
        List<LichTrinh> list = lichTrinh_dao.traCuuDSLichTrinhTheoNgay(LocalDate.now());
        list.removeIf(lt -> !lt.getThoiGianKhoiHanh().isAfter(LocalDateTime.now()));
        ObservableList<XYChart.Data<String, Integer>> data = FXCollections.observableArrayList();
        Set<String> set = new HashSet<>();
        int dem = 0;
        for (LichTrinh lt : list) {
            if (!lt.isTrangThai()) {
                continue;
            }
            if (set.contains(lt.getSoHieuTau().getSoHieuTau())) {
                continue;
            }
            data.add(new XYChart.Data<>(lt.getSoHieuTau().getSoHieuTau(), lichTrinh_dao.getSoLuongChoConTrong(lt.getMaLichTrinh()).intValue()));
            set.add(lt.getSoHieuTau().getSoHieuTau());
            dem++;
        }
        List<HoaDon> dshd = hd_dao.getHoaDonTheoNV(getData.nv.getMaNV(), LocalDate.from(LocalDateTime.now()));
        int slVe = 0;
        double doanhThu = 0;
        for (HoaDon hd : dshd) {
            if (!hd.isTrangThai()) {
                continue;
            }
            List<ChiTietHoaDon> dscthd = cthd_dao.getCT_HoaDon(hd.getMaHD());
            for (ChiTietHoaDon ct : dscthd) {
                Ve ve = ve_dao.getVeTheoID(ct.getVe().getMaVe());
                if (ve.getTinhTrangVe().equals("DaBan")) {
                    slVe++;
                }
            }
            doanhThu += hd.getTongTien();
        }
        lblSLVe.setText(slVe + "");
        lblDoanhThu.setText(DecimalFormat.getCurrencyInstance(Locale.of("vi", "VN")).format(doanhThu/1000 * 1000));
        lblSLChuyenTau.setText(dem + "");
        chartChuyenTau.getData().add(new XYChart.Series<>("Số lượng chỗ ngồi còn trống", data));
    }
    public void initDao() throws MalformedURLException, NotBoundException, RemoteException {
        lichTrinh_dao = (LichTrinhService) Naming.lookup("rmi://localhost:9999/LichTrinh_DAO");
        ve_dao = (VeService) Naming.lookup("rmi://localhost:9999/Ve_DAO");
        cthd_dao = (CT_HoaDonService) Naming.lookup("rmi://localhost:9999/CT_HoaDon_DAO");
        hd_dao = (HoaDonService) Naming.lookup("rmi://localhost:9999/HoaDon_DAO");
    }
}
