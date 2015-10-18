<?php 
//Veritabanımıza bağlanıyoruz..
mysql_connect("localhost","username","password")or die("baglanamadim"); 
mysql_query('SET NAMES utf8');
mysql_query('SET CHARACTER_SET utf8');
mysql_select_db("gps");

//İki konum arasındaki mesafeyi hesaplayan fonksiyon
function calculate_distance($lat1, $lon1, $lat2, $lon2, $unit='N') 
{ 
  $theta = $lon1 - $lon2; 
  $dist = sin(deg2rad($lat1)) * sin(deg2rad($lat2)) +  cos(deg2rad($lat1)) * cos(deg2rad($lat2)) * cos(deg2rad($theta)); 
  $dist = acos($dist); 
  $dist = rad2deg($dist); 
  $miles = $dist * 60 * 1.1515;
  $unit = strtoupper($unit);

  if ($unit == "K") {
    return ($miles * 1.609344); 
  } else if ($unit == "N") {
      return ($miles * 0.8684);
    } else {
        return $miles;
      }
}
//RESTful Web servisinde varolan kullanıcının GPS koordinat değerlerini  çekiyoruz..
$json = file_get_contents('php://input');
$obj = json_decode($json);
//Mysql veritabanımda bulunan restoranların  latitude ve longitude değerlerini ve 
//Android uygulama tarafından gelen GPS koordinat değerlerini calculate_distance fonksiyon gonderdim
$sql=mysql_query("select * from restoranLokasyon");
while($row=mysql_fetch_assoc($sql)){
//Kullanıcının konumu ile veritabanındaki restoranların konumları arasındaki mesafeleri hesapladık ve milesArray array atadım.
$milesArray[$row[id]]=calculate_distance($obj->{'latitude'},$obj->{'longitude'}, $row['latitude'], $row['longitude']);
 }
//milesArray dizisindeki uzaklık değerlerini sıraladım
asort($milesArray,SORT_NUMERIC);
$i=0;
//Kullanıcının konumuna en yakın olan yani uzaklığı en az olan 3 restoranın  latitude ve longitude değerlerini  $encode dizisine atadım
foreach ($milesArray as $key => $id) {
	$sql2=mysql_query("select * from restoranLokasyon where id='".$key."'");
	while($allRow=mysql_fetch_assoc($sql2)){
		 $new = array(
						'latitude' => $allRow['latitude'],
                        'longitude' => $allRow['longitude'],                     
						'restoran' => $allRow['restoran']                       
                    );
            $encode[] = $new;
	 }
	 if($i>2){break;}
$i++;
}
//$outputArr dizisini json ile şifreleyip(encode), Web servise gönderdim
$outputArr = array();
$outputArr['Android'] = $encode;
echo json_encode($outputArr);
mysql_close(); 
?>