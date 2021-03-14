# Bicycle

## 基本控制
利用藍芽連接自行車裝置上的藍芽裝置，通過藍芽控制自行車之自動煞車、上鎖、定速、警報靜音四個基本功能，自行車在上鎖模式啟動時會啟動裝置上的蜂鳴器，App端亦會收到通知與聲音或震動提醒。

## 定位功能
可透過設定頁面設定是否啟用定位功能與NB-IOT(註1)，啟用後，可開啟地圖頁面，透過okhttp3的GET功能與RxJava2串流資料庫經緯度資料，利用Google Map Directions API顯示自行車目前定位在地圖上。




## 註解
註1:NB-IoT是窄頻物聯網（Narrowband Internet of Things）的簡稱，是由3GPP訂定的LPWAN無線電標準，為了讓行動設備及服務的範圍可以更遠。此標準在2016年6月的3GPP Release 13定版了（LTE Advanced Pro）。其他的3GPP物聯網技術包括有eMTC（增強型機器類通信）及EC-GSM-IoT。
NB-IoT特別著重在室內的覆蓋率、低成本、長電池壽命以及高連接密度。NB-IoT使用的是長期演進技術標準的一部份，不過限制頻寬在200kHz的單一窄頻。NB-IoT使用OFDM調變來處理下行通訊，用SC-FDMA來處理上行通訊。 來源:wiki

