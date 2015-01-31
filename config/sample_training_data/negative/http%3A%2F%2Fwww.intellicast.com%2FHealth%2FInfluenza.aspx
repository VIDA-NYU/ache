
<!DOCTYPE html>
<html class="no-js" lang="en">
<head id="ctl00_ctl00_Head1"><meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" /><title>
	Intellicast - Influenza Report in United States
</title><meta name="viewport" content="width=device-width,initial-scale=1" /><meta http-equiv="Content-Type" content="text/html; charset=utf-8" /><meta name="description" content="Intellicast.com: The Authority in Expert Weather" /><meta name="google-site-verification" content="Ty8E9HwyYpwnoH6SWxOoDkwWIpfXZbnXYxSKIJKinLw" /><meta name="google-site-verification" content="OwqzWim3JgPf7WONV8sYyMir93oTT-JmWDKQW-IApCI" /><meta name="verify-v1" content="bwhh8UmywoDXIeujVrNoVWPt5FmUdjfYgs0MgjMjouc=" /><meta name="msvalidate.01" content="2625493FFF704B18B25610DED3DC742E" /><meta name="y_key" content="66d7db7fca59b234" /><meta http-equiv="PICS-Label" content="(PICS-1.1 &quot;http://www.rsac.org/ratingsv01.html&quot; l gen true comment &quot;RSACi North America Server&quot; for &quot;http://www.intellicast.com&quot; on &quot;1998.08.31T21:19-0800&quot; r (n 0 s 0 v 0 l 0))&quot;" /><meta http-equiv="cache-control" content="no-cache" /><meta http-equiv="expires" content="0" /><meta http-equiv="pragma" content="no-cache" /><link rel="shortcut icon" href="/favicon.ico" type="image/x-icon" /><link rel="icon" href="/favicon.ico" type="image/x-icon" /><link href="http://images.intellicast.com/Styles/normalize.rexive.css" rel="stylesheet" /><link href="http://images.intellicast.com/Styles/main_20140521.css" rel="stylesheet" /><link href="http://images.intellicast.com/Styles/Stylesheet_20131220.css" rel="stylesheet" />


	
  <script src="//icons.wxug.com/scripts/modernizr/2.8.2/modernizr.min.js"></script>

	<!-- BEGIN New Ads -->
	<script src="/Scripts/Vendor/twc-jquery-1.10.2.min.js"></script>
	<script type="text/javascript" src="http://d.imwx.com/services/pco"></script>
	<script type="text/javascript" src="http://js.revsci.net/gateway/gw.js?csid=K06578"></script>
	<script type="text/javascript">
		// set global locationId
		var locationId = '';
	</script>

  <!-- BEGIN Criteo/WxEffects -->
  <script type="text/javascript">
    var crtg_nid = "2305";
    var crtg_cookiename = "cto_weather";
    var crtg_varname = "crtg_content";
    function crtg_getCookie(c_name) { var i, x, y, ARRCookies = document.cookie.split(";"); for (i = 0; i < ARRCookies.length; i++) { x = ARRCookies[i].substr(0, ARRCookies[i].indexOf("=")); y = ARRCookies[i].substr(ARRCookies[i].indexOf("=") + 1); x = x.replace(/^\s+|\s+$/g, ""); if (x == c_name) { return unescape(y); } } return ''; }
    var crtg_content = crtg_getCookie(crtg_cookiename); var crtg_rnd = Math.floor(Math.random() * 99999999999);
    var crtg_url = location.protocol + '//rtax.criteo.com/delivery/rta/rta.js?netId=' + escape(crtg_nid); crtg_url += '&cookieName=' + escape(crtg_cookiename); crtg_url += '&rnd=' + crtg_rnd; crtg_url += '&varName=' + escape(crtg_varname);
    var crtg_script = document.createElement('script'); crtg_script.type = 'text/javascript'; crtg_script.src = crtg_url; crtg_script.async = true;
    if (document.getElementsByTagName("head").length > 0) document.getElementsByTagName("head")[0].appendChild(crtg_script); else if (document.getElementsByTagName("body").length > 0) document.getElementsByTagName("body")[0].appendChild(crtg_script);
  </script>

  <!-- BEGIN Wxfx -->
  <script src ="http://triggers.weather.com/js/?resp_type=kv"></script>
  <!-- END Wxfx -->

  <!-- BEGIN Lotame TPID -->
  <script async src="http://tags.crwdcntrl.net/c/2215/cc.js?ns=_cc2215" id="LOTCC_2215"></script>
  <script>
    (function ($) {
      $(document).ready(function () {
        var url1 = 'http://ad.crwdcntrl.net/5/c=2215/pe=y/callback=?'; $.ajax({
          url: url1, dataType: 'jsonp', cache: true, type: 'GET', timeout: 5000, success: function (data) {
            if (TWC.pco.get('user.lotame') == null || typeof (TWC.pco.get('user.lotame')) == "undefined") { TWC.pco.setUser('lotame', {}); }
            var lot_tpid, lot_abbr, lot_id, lotame; lotame = TWC.pco.get('user.lotame'); lot_tpid = lotame.lot_tpid; lot_abbr = lotame.lot_abbr; lot_id = lotame.lot_id; lotame.lot_tpid = data.Profile.tpid; var audienceLength = data.Profile.Audiences.Audience.length; if (audienceLength > 0) {
              lot_abbr = ''; lot_id = ''; for (var cci = 0; cci < audienceLength; cci++) { lot_abbr += (data.Profile.Audiences.Audience[cci].abbr + (cci < audienceLength - 1 ? ',' : '')); lot_id += (data.Profile.Audiences.Audience[cci].id + (cci < audienceLength - 1 ? ',' : '')); }
              lotame.lot_abbr = lot_abbr; lotame.lot_id = lot_id;
            }
            TWC.pco.setUser('lotame', lotame); if (_cc2215) {
              if (TWC.pco.get("user") != null && typeof (TWC.pco.get("user")) != "undefined" && TWC.pco.get("user.preferredLocation")) {
                var u = checkCookie(); _cc2215.add("age", u); _cc2215.add("gen", TWC.pco.get("user").gender); if (TWC.pco.get("metrics") != null && typeof (TWC.pco.get("metrics")) != "undefined") { _cc2215.add("seg", "level1_" + TWC.pco.get("metrics").level1); _cc2215.add("seg", "level2_" + TWC.pco.get("metrics").level2); _cc2215.add("seg", "level3_" + TWC.pco.get("metrics").level3); _cc2215.add("seg", "level4_" + TWC.pco.get("metrics").level4); }
                _cc2215.add("seg", "claritas_" + TWC.pco.get("user").claritas); _cc2215.add("seg", "preferredDeclaration_" + TWC.pco.get("user").preferredDeclaration); if (TWC.pco.get("user.hardDeclarations") != null && typeof (TWC.pco.get("user.hardDeclarations")) != "undefined") { if (TWC.pco.get("user").hardDeclarations.length > 0) { _cc2215.add("seg", "hardDeclarations_" + TWC.pco.get("user").hardDeclarations[0]); } }
                if (TWC.pco.get("user.softDeclarations") != null && typeof (TWC.pco.get("user.softDeclarations")) != "undefined") { if (TWC.pco.get("user").softDeclarations.length > 0) { _cc2215.add("seg", "softDeclarations_" + TWC.pco.get("user").softDeclarations[0]); } }
                _cc2215.add("seg", "preferredLocation_locid_" + TWC.pco.get("user").preferredLocation.locid); _cc2215.add("seg", "preferredLocation_dma_" + TWC.pco.get("user").preferredLocation.dma); if (TWC.pco.get("user.recentSearchLocations") != null && typeof (TWC.pco.get("user.recentSearchLocations")) != "undefined") { if (TWC.pco.get("user").recentSearchLocations.length > 0) { _cc2215.add("seg", "recentSearchLocations_locid_" + TWC.pco.get("user").recentSearchLocations[0].locid); _cc2215.add("seg", "recentSearchLocations_dma_" + TWC.pco.get("user").recentSearchLocations[0].dma); } }
              }
              if (TWC.pco.get("currloc") != null && typeof (TWC.pco.get("currloc")) != "undefined") { _cc2215.add("seg", "zip_" + TWC.pco.get("currloc").zip); _cc2215.add("seg", "locid_" + TWC.pco.get("currloc").locid); _cc2215.add("seg", "dma_" + TWC.pco.get("currloc").dma); _cc2215.add("seg", "claritas_" + TWC.pco.get("currloc").claritas); _cc2215.add("seg", "state_" + TWC.pco.get("currloc").state); _cc2215.add("seg", "locTimeZoneAbbr_" + TWC.pco.get("currloc").locTimeZoneAbbr); _cc2215.add("seg", "city_" + TWC.pco.get("currloc").city); _cc2215.add("seg", "country_" + TWC.pco.get("currloc").country); _cc2215.add("seg", "loctype_" + TWC.pco.get("currloc").loctype); _cc2215.add("seg", "locname_" + TWC.pco.get("currloc").locname); }
              if (TWC.pco.get("page") != null && typeof (TWC.pco.get("page")) != "undefined") { _cc2215.add("seg", "lang_" + TWC.pco.get("page").lang); _cc2215.add("seg", "ampm_" + TWC.pco.get("page").ampm); }
              if (TWC.pco.get("ad") != null && typeof (TWC.pco.get("ad")) != "undefined") {
                if (TWC.pco.get("ad.site") != null && typeof (TWC.pco.get("ad.site")) != "undefined") { _cc2215.add("seg", "site_" + TWC.pco.get("ad").site); }
                if (TWC.pco.get("ad.pageIdCode") != null && typeof (TWC.pco.get("ad.pageIdCode")) != "undefined") { _cc2215.add("seg", "pageIdCode_" + TWC.pco.get("ad").pageIdCode); }
                if (TWC.pco.get("ad.mode") != null && typeof (TWC.pco.get("ad.mode")) != "undefined") { _cc2215.add("seg", "mode_" + TWC.pco.get("ad").mode); }
                if (TWC.pco.get("ad.pageDeclaration") != null && typeof (TWC.pco.get("ad.pageDeclaration")) != "undefined") { _cc2215.add("seg", "pageDeclaration_" + TWC.pco.get("ad").pageDeclaration); }
                if (TWC.pco.get("ad.zone") != null && typeof (TWC.pco.get("ad.zone")) != "undefined") { _cc2215.add("seg", "zone_" + TWC.pco.get("ad").zone); }
              }
              if (TWC.pco.get("wx") != null && typeof (TWC.pco.get("wx")) != "undefined") {
                _cc2215.add("seg", "wind_" + TWC.pco.get("wx").wind); _cc2215.add("seg", "hum_" + TWC.pco.get("wx").hum); _cc2215.add("seg", "relativeHumidity_" + TWC.pco.get("wx").relativeHumidity); _cc2215.add("seg", "cwsh_" + TWC.pco.get("wx").fcast.cwsh); _cc2215.add("seg", "cwsl_" + TWC.pco.get("wx").fcast.cwsl); _cc2215.add("seg", "fsnw_" + TWC.pco.get("wx").fcast.fsnw); _cc2215.add("seg", "tempH_" + TWC.pco.get("wx").fcast.tempH); _cc2215.add("seg", "tempHR_" + TWC.pco.get("wx").fcast.tempHR); _cc2215.add("seg", "tempL_" + TWC.pco.get("wx").fcast.tempL); _cc2215.add("seg", "tempLR_" + TWC.pco.get("wx").fcast.tempLR); _cc2215.add("seg", "fc1_" + TWC.pco.get("wx").fcast.cond.fc1); _cc2215.add("seg", "fc2_" + TWC.pco.get("wx").fcast.cond.fc2); _cc2215.add("seg", "fc3_" + TWC.pco.get("wx").fcast.cond.fc3); _cc2215.add("seg", "cond_" + TWC.pco.get("wx").cond); _cc2215.add("seg", "snw_" + TWC.pco.get("wx").snw); _cc2215.add("seg", "tempR_" + TWC.pco.get("wx").tempR); _cc2215.add("seg", "windSpeed_" + TWC.pco.get("wx").windSpeed); _cc2215.add("seg", "pollen_" + TWC.pco.get("wx").pollen); _cc2215.add("seg", "baroTendency_" + TWC.pco.get("wx").baroTendency); _cc2215.add("seg", "uv_" + TWC.pco.get("wx").uv); _cc2215.add("seg", "uvIndex_" + TWC.pco.get("wx").uvIndex); _cc2215.add("seg", "temp_" + TWC.pco.get("wx").temp); if (TWC.pco.get("wx.severe") != null && typeof (TWC.pco.get("wx.severe")) != "undefined") { if (TWC.pco.get("wx").severe.length > 0) { _cc2215.add("seg", "severe_" + TWC.pco.get("wx").severe[0]); } }
                _cc2215.add("seg", "realTemp_" + TWC.pco.get("wx").realTemp);
              }
              _cc2215.bcp();
            }
          }, error: function (jqXHR, textStatus, errorThrown) { }
        });
      });
      function checkCookie() {
      	var userpref = getCookie("UserPreferences"); if (userpref != null && userpref != "")
      	{ var i = userpref.split("|"); var d = new Date(); var n = d.getFullYear(); j = n - i[13].valueOf(); if (j > 1000) { j = "no_age_available"; } }
      	else
      	{ j = "no_age_available"; }
      	return j;
      }
    })(jQuery);
    function getCookie(c_name) {
    	var i, x, y, ARRcookies = document.cookie.split(";"); for (i = 0; i < ARRcookies.length; i++) {
    		x = ARRcookies[i].substr(0, ARRcookies[i].indexOf("=")); y = ARRcookies[i].substr(ARRcookies[i].indexOf("=") + 1); x = x.replace(/^\s+|\s+$/g, ""); if (x == c_name)
    		{ return unescape(y); }
    	}
    }
  </script>
  <!-- END Lotame TPID -->

  <!-- Yieldfire call. -->
  <!-- YF Enabled: true -->
 
  <!-- END Criteo/WxEffects -->

  <!-- BEGIN Amazon -->
  <script type='text/javascript' src='http://c.amazon-adsystem.com/aax2/amzn_ads.js'></script> 
  <script type='text/javascript'> 
    try {
      amznads.getAds('1004'); 
    } catch(e) { /*ignore*/} 
  </script> 
  <!-- END Amazon -->

	<script type="text/javascript" src="http://images.intellicast.com/Scripts/ads_20130111.js"></script>
	<script type="text/javascript" src="http://d.imwx.com/managedfe/js/TWC/util/a21_dfpp.js"></script>
	
  <!-- END New Ads -->

  <!-- Optimizely -->
  <script src="//cdn.optimizely.com/js/878051086.js"></script>

  <!-- BEGIN Omniture -->
  <script src="http://www.intellicast.com/Scripts/AppMeasurement.js"></script>
  <!-- END Omniture -->
	
  <script src="http://images.intellicast.com/App_Scripts/icast.storage.min.js"></script>
	<script src="http://images.intellicast.com/App_Scripts/jquery-autocomplete.min.js"></script>
	
	<script type="text/javascript">
		var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
		document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
	</script>
	<script type="text/javascript">
		var linkTracker = _gat._getTracker("UA-17339464-1");
	</script>
	

  <meta name="description" content="The Influenza Report map shows areal coverage of the flu in each state for the current day." />
  <meta name="keywords" content="weather, radar, satellite, local, national, global, storm, rain, snow, temperature" />

<!-- New Ads -->
<style type="text/css">
  .img-link{disp shows areal coverage of the flu in each state for the current day." lay:block;margin:10px 0;}
  .img-link img{border:none;display:block;}
</style>
<!-- New Ads -->

	<!-- HTML5 Shim: IE8 support of HTML5 elements -->
  <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
  <![endif]-->
</head>
<body id="ctl00_ctl00_MasterBody">
	

	<!-- Begin comScore Tag -->
	<script type="text/javascript">var _comscore = _comscore || []; _comscore.push({ c1: "2", c2: "9576127", c3: "", c4: window.location.href }); (function () { var s = document.createElement("script"), el = document.getElementsByTagName("script")[0]; s.async = true; s.src = (document.location.protocol == "https:" ? "https://sb" : "http://b") + ".scorecardresearch.com/beacon.js"; el.parentNode.insertBefore(s, el); })();</script>
	<noscript><img src="http://b.scorecardresearch.com/p?c1=2&c2=9576127&c3=&c4=www.intellicast.com/Health/Influenza.aspx&c5=&c6=&c15=&cv=2.0&cj=1" alt="" style="position:absolute;top:-1000px;" /></noscript>
	<!-- End comScore Tag -->
	
	<header id="header" class="site-header">
		<div class="container">
			<div class="row">
				<div class="logo-wrapper">
					<a href="/" class="logo">
						<span class="sr-only">Intellicast.com</span>
						<span class="slogan">The Authority in Expert Weather</span>
					</a>
				</div>
				<div class="user-tools">
					<div class="constants">
						<a class="user" href="/Members/Default.aspx"><i class="icon"></i>Profile</a>
						<a class="alerts" href="/Storm/Severe/Bulletins.aspx"><i class="icon"></i>Alerts</a>
						<a class="hurricane""" href="/Storm/Hurricane/Track.aspx"><i class="icon"></i>Hurricanes</a>
						<a class="help" href="/Help/Default.aspx"><i class="icon"></i>Help</a>
						<a class="inav inav-toggle toggle-btn" href="/Members/INav.aspx"><i class="icon"></i>iNav</a>
					</div>
					<div class="inav-flyout toggle-target">
						
							<a href="/Members/Login.aspx">Sign-In to Add to iNav</a>
						
					</div>
					<form method="get" action="/Local/Default.aspx" class="ui-front location-search">
						<input tabindex="1" class="blurred" id="locSearch" name="query" type="text" value="Enter City, State, Country or U.S. Zip code" autocomplete="off" />
					</form>
				</div>
			</div>
			<div class="row">
				<div class="site-nav">
					<ul class="nav">
						<li><a href="/Local/Weather.aspx" class="toggle-btn">Local</a>
							<ul>
								<li><a href="/Local/Default.aspx">Find A Location</a></li>
								<li><a href="/Local/Weather.aspx">Weather Report</a></li>
								<li><a href="/Local/WxMap.aspx">Interactive Weather Map</a></li>
								<li><a href="/Local/Forecast.aspx">Extended Forecast</a></li>
								<li><a href="/Local/Hourly.aspx">Hourly Forecast</a></li>
								<li><a href="/Local/Observation.aspx">Past Observations</a></li>
								<li><a href="/Local/ObservationsSummary.aspx">Past Observations Summary</a></li>
								<li><a href="/Local/History.aspx">Historic Averages</a></li>
							</ul>
						</li>
						<li><a href="/Local/WxMap.aspx" class="toggle-btn">Interactive</a>
							<ul>
								<li><a href="/Local/WxMap.aspx">Interactive Weather Map</a></li>
								<li><a href="/Marine/Boating.aspx">Interactive Boating Map</a></li>
								<li><a href="/Local/WxMapSm.aspx">Interactive Map (Small)</a></li>
								<li><a href="/Local/MapFeedback.aspx">Map Feedback</a></li>
							</ul>
						</li>
						<li><a href="/National/Default.aspx" class="toggle-btn">National</a>
							<ul>
								<li><a href="/National/Default.aspx">National Home</a></li>
								<li><a href="/National/Weather.aspx">Current Weather</a></li>
								<li><a href="/National/ForecastToday.aspx">Today's Forecast</a></li>
								<li><a href="/National/ForecastTomorrow.aspx">Tomorrow's Forecast</a></li>
								<li><a href="/National/Radar/Default.aspx">Radar<i class="caret"></i></a>
									<ul>
										<li><a href="/National/Radar/Current.aspx">Current Radar</a></li>
										<li><a href="/National/Radar/Forecast.aspx">Forecast Radar</a></li>
										<li><a href="/National/Radar/Summary.aspx">Radar Summary</a></li>
										<li><a href="/National/Radar/Yesterday.aspx">Yesterday's Radar Loop</a></li>
										<li><a href="/National/Radar/OneKM.aspx">1Km Radar</a></li>
										<li><a href="/National/Radar/Metro.aspx">Metro Radar</a></li>
									</ul>
								</li>
								<li><a href="/National/Nexrad/Default.aspx">Nexrad<i class="caret"></i></a>
									<ul>
										<li><a href="/National/Nexrad/BaseReflectivity.aspx">Base Reflectivity</a></li>
										<li><a href="/National/Nexrad/RadialVelocity.aspx">Radial Velocity</a></li>
									</ul>
								</li>
								<li><a href="/National/Surface/Default.aspx">Surface Analysis<i class="caret"></i></a>
									<ul>
										<li><a href="/National/Surface/Current.aspx">Current Surface Analysis</a></li>
										<li><a href="/National/Surface/Mixed.aspx">Mixed Surface Analysis</a></li>
										<li><a href="/National/Surface/Forecast12.aspx">12-Hour Surface Forecast</a></li>
										<li><a href="/National/Surface/Forecast24.aspx">24-Hour Surface Forecast</a></li>
										<li><a href="/National/Surface/Forecast36.aspx">36-Hour Surface Forecast</a></li>
										<li><a href="/National/Surface/Forecast48.aspx">48-Hour Surface Forecast</a></li>
									</ul>
								</li>
								<li><a href="/National/Satellite/Default.aspx">Satellite<i class="caret"></i></a>
									<ul>
										<li><a href="/National/Satellite/Infrared.aspx">National Infrared Satellite</a></li>
										<li><a href="/National/Satellite/Regional.aspx">Regional Infrared Satellite</a></li>
										<li><a href="/National/Satellite/Visible.aspx">Visible Satellite</a></li>
										<li><a href="/National/Satellite/WaterVapor.aspx">Water Vapor</a></li>
									</ul>
								</li>
								<li><a href="/National/Temperature/Default.aspx">Temperature<i class="caret"></i></a>
									<ul>
										<li><a href="/National/Temperature/Current.aspx">Current Temperatures</a></li>
										<li><a href="/National/Temperature/HighToday.aspx">High Temperatures Today</a></li>
										<li><a href="/National/Temperature/HighTomorrow.aspx">High Temperatures Tomorrow</a></li>
										<li><a href="/National/Temperature/LowTomorrow.aspx">Low Temperatures Tomorrow</a></li>
										<li><a href="/National/Temperature/TEMPcast.aspx">TEMPcast</a></li>
										<li><a href="/National/Temperature/WindChill.aspx">Wind Chill</a></li>
										<li><a href="/National/Temperature/Extreme.aspx">Feels Like Temps</a></li>
										<li><a href="/National/Temperature/Departure.aspx">Temp Departure</a></li>
										<li><a href="/National/Temperature/Departure10.aspx">10 Day Temp Departure</a></li>
										<li><a href="/National/Temperature/Departure30.aspx">30 Day Temp Departure</a></li>
										<li><a href="/National/Temperature/Departure90.aspx">90 Day Temp Departure</a></li>
										<li><a href="/National/Temperature/FrostFreeze.aspx">Frost &amp; Freeze</a></li>
										<li><a href="/National/Temperature/FirstLastFreeze.aspx">First &amp; Last Freeze</a></li>
										<li><a href="/National/Temperature/Delta.aspx">24hr Deltas</a></li>
									</ul>
								</li>
								<li><a href="/National/Outdoors/Default.aspx">Outdoors<i class="caret"></i></a>
									<ul>
										<li><a href="/National/Outdoors/SOLARcast.aspx">SOLARcast</a></li>
										<li><a href="/National/Outdoors/Summer.aspx">Summer Fun</a></li>
										<li><a href="/National/Outdoors/Gardening.aspx">Gardening</a></li>
										<li><a href="/National/Outdoors/Watering.aspx">Plant Watering</a></li>
										<li><a href="/National/Outdoors/Painting.aspx">Painting</a></li>
										<li><a href="/National/Outdoors/Golfing.aspx">Golfing</a></li>
										<li><a href="/National/Outdoors/Turf.aspx">Golf Turf Conditions</a></li>
										<li><a href="/National/Outdoors/UltraViolet.aspx">UV Report</a></li>
										<li><a href="/National/Outdoors/Sky.aspx">Sky Watch</a></li>
									</ul>
								</li>
								<li><a href="/National/Precipitation/Default.aspx">Precipitation<i class="caret"></i></a>
									<ul>
										<li><a href="/National/Precipitation/PrecipCast.aspx">PRECIPcast</a></li>
										<li><a href="/National/Precipitation/RainCast.aspx">RAINcast</a></li>
										<li><a href="/National/Precipitation/PopCast.aspx">POPcast</a></li>
										<li><a href="/National/Precipitation/Estimated.aspx">Estimated Rainfall</a></li>
										<li><a href="/National/Precipitation/Daily.aspx">Daily Precipitation</a></li>
										<li><a href="/National/Precipitation/Weekly.aspx">Weekly Precipitation</a></li>
										<li><a href="/National/Precipitation/Departure10.aspx">10 Day Precip Departure</a></li>
										<li><a href="/National/Precipitation/Departure30.aspx">30 Day Precip Departure</a></li>
										<li><a href="/National/Precipitation/Departure90.aspx">90 Day Precip Departure</a></li>
									</ul>
								</li>
								<li><a href="/National/Wind/Default.aspx">Wind<i class="caret"></i></a>
									<ul>
										<li><a href="/National/Wind/Current.aspx">Current Winds</a></li>
										<li><a href="/National/Wind/WINDcast.aspx">WINDcast</a></li>
										<li><a href="/National/Wind/JetStream.aspx">Jet Stream</a></li>
									</ul>
								</li>
								<li><a href="/National/Humidity/Default.aspx">Humidity<i class="caret"></i></a>
									<ul>
										<li><a href="/National/Humidity/Current.aspx">Current Humidity</a></li>
										<li><a href="/National/Humidity/IndoorRelative.aspx">Indoor Relative Humidity</a></li>
										<li><a href="/National/Humidity/HUMIDITYcast.aspx">HUMIDITYcast</a></li>
										<li><a href="/National/Humidity/FOGcast.aspx">FOGcast</a></li>
										<li><a href="/National/Humidity/DewPoint.aspx">Dew Point</a></li>
									</ul>
								</li>
								<li><a href="/National/Analysis/Default.aspx">Analysis Charts<i class="caret"></i></a>
									<ul>
										<li><a href="/National/Analysis/SuperFax.aspx">WSI SuperFax</a></li>
										<li><a href="/National/Analysis/UpperAir300.aspx">300MB Upper Air</a></li>
										<li><a href="/National/Analysis/UpperAir500.aspx">500MB Upper Air</a></li>
										<li><a href="/National/Analysis/UpperAir850.aspx">850MB Upper Air</a></li>
										<li><a href="/National/Analysis/Difax24.aspx">24hr Difax</a></li>
										<li><a href="/National/Analysis/Difax48.aspx">48hr Difax</a></li>
										<li><a href="/National/Analysis/Difax72.aspx">72hr Difax</a></li>
									</ul>
								</li>
							</ul>
						</li>
						<li><a href="/Global/Default.aspx" class="toggle-btn">Global</a>
							<ul>
								<li><a href="/Global/Default.aspx">Global Home</a></li>
								<li><a href="/Global/Humidity.aspx">Relative Humidity</a></li>
								<li><a href="/Global/Surface.aspx">Surface Analysis</a></li>
								<li><a href="/Global/Satellite/Default.aspx">Satellite<i class="caret"></i></a>
									<ul>
										<li><a href="/Global/Satellite/Current.aspx">Current Satellite</a></li>
										<li><a href="/Global/Satellite/Infrared.aspx">Infrared Satellite</a></li>
									</ul>
								</li>
								<li><a href="/Global/Temperature/Default.aspx">Temperature<i class="caret"></i></a>
									<ul>
										<li><a href="/Global/Temperature/Current.aspx">Current Temperatures</a></li>
										<li><a href="/Global/Temperature/Minimum.aspx">Minimum Temperatures</a></li>
										<li><a href="/Global/Temperature/Maximum.aspx">Maximum Temperatures</a></li>
										<li><a href="/Global/Temperature/Sunshine.aspx">Sunshine</a></li>
									</ul>
								</li>
								<li><a href="/Global/Precipitation/Default.aspx">Precipitation<i class="caret"></i></a>
									<ul>
										<li><a href="/Global/Precipitation/Current.aspx">Current Precipitation</a></li>
										<li><a href="/Global/Precipitation/ForecastAM.aspx">AM Precipitation Forecast</a></li>
										<li><a href="/Global/Precipitation/ForecastPM.aspx">PM Precipitation Forecast</a></li>
									</ul>
								</li>
								<li><a href="/Global/Wind/Default.aspx">Wind<i class="caret"></i></a>
									<ul>
										<li><a href="/Global/Wind/Current.aspx">Current Winds</a></li>
										<li><a href="/Global/Wind/Forecast.aspx">Forecast Winds</a></li>
									</ul>
								</li>
							</ul>
						</li>
						<li><a href="/Storm/Default.aspx" class="toggle-btn">Storms</a>
							<ul>
								<li><a href="/Storm/Default.aspx">Storm Home</a></li>
								<li><a href="/Storm/Severe/Default.aspx">Severe Weather<i class="caret"></i></a>
									<ul>
										<li><a href="/Storm/Severe/Bulletins.aspx">Weather Alerts</a></li>
										<li><a href="/Storm/Severe/WatchesWarnings.aspx">Watches &amp; Warnings</a></li>
										<li><a href="/Storm/Severe/OneKM.aspx">1Km Storm Watch</a></li>
										<li><a href="/Storm/Severe/Metro.aspx">Metro Storm Watch</a></li>
										<li><a href="/Storm/Severe/OutlookToday.aspx">Severe Outlook Today</a></li>
										<li><a href="/Storm/Severe/OutlookTomorrow.aspx">Severe Outlook Tomorrow</a></li>
										<li><a href="/Storm/Severe/Lightning.aspx">Lightning Strikes</a></li>
										<li><a href="/Storm/Severe/ThunderCast.aspx">THUNDERcast</a></li>
										<li><a href="/Storm/Severe/WaterVapor.aspx">Water Vapor</a></li>
									</ul>
								</li>
								<li><a href="/Storm/Hurricane/Default.aspx">Hurricane<i class="caret"></i></a>
									<ul>
										<li><a href="/Storm/Hurricane/Track.aspx">Active Track</a></li>
										<li><a href="/Storm/Hurricane/TropicalWinds24.aspx">24hr Tropical Winds</a></li>
										<li><a href="/Storm/Hurricane/TropicalWinds48.aspx">48hr Tropical Winds</a></li>
										<li><a href="/Storm/Hurricane/TropicalWinds72.aspx">72hr Tropical Winds</a></li>
										<li><a href="/Storm/Hurricane/AtlanticAnalysis.aspx">Atlantic Analysis</a></li>
										<li><a href="/Storm/Hurricane/AtlanticSatellite.aspx">Atlantic Satellite</a></li>
										<li><a href="/Storm/Hurricane/AtlanticForecast.aspx">Atlantic Forecast</a></li>
										<li><a href="/Storm/Hurricane/CaribbeanSatellite.aspx">Caribbean Satellite</a></li>
										<li><a href="/Storm/Hurricane/PacificAnalysis.aspx">Pacific Analysis</a></li>
										<li><a href="/Storm/Hurricane/PacificSatellite.aspx">Pacific Satellite</a></li>
									</ul>
								</li>
								<li><a href="/Storm/Summary/Default.aspx">Season Summaries<i class="caret"></i></a>
									<ul>
										<li><a href="/Storm/Summary/Hurricane2013.aspx">2013 Hurricane Summary</a></li>
                    <li><a href="/Storm/Summary/Hurricane2012.aspx">2012 Hurricane Summary</a></li>
										<li><a href="/Storm/Summary/Hurricane2011.aspx">2011 Hurricane Summary</a></li>
										<li><a href="/Storm/Summary/Hurricane2010.aspx">2010 Hurricane Summary</a></li>
										<li><a href="/Storm/Summary/Hurricane2009.aspx">2009 Hurricane Summary</a></li>
										<li><a href="/Storm/Summary/Hurricane2008.aspx">2008 Hurricane Summary</a></li>
										<li><a href="/Storm/Summary/Hurricane2007.aspx">2007 Hurricane Summary</a></li>
										<li><a href="/Storm/Summary/Hurricane2006.aspx">2006 Hurricane Summary</a></li>
										<li><a href="/Storm/Summary/Hurricane2005.aspx">2005 Hurricane Summary</a></li>
										<li><a href="/Storm/Summary/Hurricane2004.aspx">2004 Hurricane Summary</a></li>
										<li><a href="/Storm/Summary/Hurricane2003.aspx">2003 Hurricane Summary</a></li>
										<li><a href="/Storm/Summary/Hurricane2002.aspx">2002 Hurricane Summary</a></li>
										<li><a href="/Storm/Summary/Hurricane2001.aspx">2001 Hurricane Summary</a></li>
										<li><a href="/Storm/Summary/Hurricane2000.aspx">2000 Hurricane Summary</a></li>
										<li><a href="/Storm/Summary/Hurricane1999.aspx">1999 Hurricane Summary</a></li>
										<li><a href="/Storm/Summary/Hurricane1998.aspx">1998 Hurricane Summary</a></li>
									</ul>
								</li>
							</ul>
						</li>
						<li><a href="/Health/Default.aspx" class="toggle-btn">Health</a>
							<ul>
								<li><a href="/Health/Default.aspx">Health Home</a></li>
								<li><a href="/Health/AchesPains.aspx">Aches &amp; Pains</a></li>
								<li><a href="/Health/Air.aspx">Air Stagnation</a></li>
								<li><a href="/Health/Attentiveness.aspx">Attentiveness</a></li>
								<li><a href="/Health/BadHairDay.aspx">Bad Hair Day</a></li>
								<li><a href="/Health/Influenza.aspx">Influenza Report</a></li>
								<li><a href="/Health/Labor.aspx">Labor Index</a></li>
								<li><a href="/Health/Mood.aspx">Mood Index</a></li>
								<li><a href="/Health/Reflex.aspx">Reflex Times</a></li>
								<li><a href="/Health/Respiratory.aspx">Respiratory Distress</a></li>
							</ul>
						</li>
						<li><a href="/Travel/Default.aspx" class="toggle-btn">Travel</a>
							<ul>
								<li><a href="/Travel/Default.aspx">Travel Home</a></li>
								<li><a href="/Travel/Risk.aspx">Delay Risk</a></li>
								<li><a href="/Travel/Outlook.aspx">Outlook</a></li>
								<li><a href="/Travel/Flying/Default.aspx">Airport Delays</a></li>
								<li><a href="/Travel/Library/Browse.aspx">Climate Guides</a></li>
								<li><a href="/Travel/Driving/Road.aspx">Driving<i class="caret"></i></a>
									<ul>
										<li><a href="/Travel/Driving/Highways.aspx">Highway Conditions</a></li>
										<l<a href="/Travel/Library/Browse.aspx">Climate Guides</a></li>
								<li><a href="/Travel/Driving/Road.aspx">Driving<i class="caret"></i></a>
									<ul>
										i><a href="/Travel/Driving/Road.aspx">Roadway Conditions</a></li>
										<li><a href="/Travel/Driving/TopUpTopDown.aspx">Top Up Top Down</a></li>
									</ul>
								</li>
								<li><a href="/Travel/Weather/Snow/Default.aspx">Ski &amp; Snow<i class="caret"></i></a>
									<ul>
										<li><a href="javascript:internalPageLink('/Local/WxMap.aspx?latitude=39&longitude=-97&zoomLevel=4&opacity=1&basemap=0014&layers=0053');">Interactive Snow Cover</a></li>
										<li><a href="/Travel/Weather/Snow/Ski.aspx">Ski Conditions</a></li>
										<li><a href="/Travel/Weather/Snow/Cover.aspx">Snow Cover</a></li>
										<li><a href="/Travel/Weather/Snow/SNOWcast.aspx">SNOWcast</a></li>
										<li><a href="/Travel/Weather/Snow/Forecast.aspx">48hr Snow Forecast</a></li>
										<li><a href="/Travel/Weather/Snow/Storm.aspx">24hr Snow Forecast</a></li>
									</ul>
								</li>
								<li><a href="/Travel/Weather/Marine/Default.aspx">Boat &amp; Beach<i class="caret"></i></a>
									<ul>
										<li><a href="/Marine/Boating.aspx">Interactive Boating Map</a></li>
										<li><a href="/Travel/Weather/Marine/Sailing.aspx">Great Sailing</a></li>
										<li><a href="/Travel/Weather/Marine/Water.aspx">Water Temperatures</a></li>
										<li><a href="/Travel/Weather/Marine/Waves.aspx">Wave Heights</a></li>
	
										<li><a href="/Travel/Weather/Marine/Sailing.aspx">Great Sailing</a></li>
										<li><a href="/Travel/Weather/Marine/Water.aspx">Water Temperatures</a></li									<li><a href="/Travel/Weather/Marine/Forecast.aspx">Marine Forecast</a></li>
									</ul>
								</li>
							</ul>
						</li>
						<li><a href="/National/ForecastToday.aspx" class="toggle-btn">Forecast</a>
							<ul>
								<li><a href="/Local/Weather.aspx">10-Day Weather Report</a></li>
								<li><a href="/Local/Hourly.aspx">Hourly</a></li>
								<li><a href="/National/Radar/Forecast.aspx">Radar</a></li>
								<li><a href="/National/Surface/Forecast12.aspx">Surface</a></li>
								<li><a href="/National/Temperature/TEMPcast.aspx">Temperature</a></li>
								<li><a href="/National/Precipitation/PrecipCast.aspx">Precipitation</a></li>
								<li><a href="/National/Precipitation/RainCast.aspx">Rain</a></li>
								<li><a href="/Storm/Severe/ThunderCast.aspx">Thunder</a></li>
								<li><a href="/National/Wind/WINDcast.aspx">Winds</a></li>
								<li><a href="/National/Humidity/HUMIDITYcast.aspx">Humidity</a></li>
							</ul>
						</li>
						<li><a href="/National/Weather.aspx" class="toggle-btn">Current</a>
							<ul>
								<li><a href="/National/Radar/Current.aspx">Radar</a></li>
								<li><a href="/National/Surface/Current.aspx">Surface</a></li>
								<li><a href="/National/Temperature/Current.aspx">Temperatures</a></li>
								<li><a href="/National/Temperature/Delta.aspx">Temperature Changes</a></li>
								<li><a href="/National/Precipitation/Estimated.aspx">Precipitation</a></li>
								<li><a href="/National/Wind/Current.aspx">Winds</a></li>
								<li><a href="/National/Humidity/Current.aspx">Humidity</a></li>
							</ul>
						</li>
						<li><a href="/National/Radar/Default.aspx" class="toggle-btn">Radar</a>
							<ul>
								<li><a href="/Local/WxMap.aspx">Interactive Radar</a></li>
								<li><a href="/National/Radar/Current.aspx">Current Radar</a></li>
								<li><a href="/National/Radar/Current.aspx?animate=true">Current Loops</a></li>
								<li><a href="/National/Radar/Forecast.aspx">Forecast</a></li>
								<li><a href="/National/Radar/OneKM.aspx">Regional (1Km)</a></li>
								<li><a href="/National/Radar/OneKM.aspx?animate=true">Regional (1Km) Loop</a></li>
								<li><a href="/National/Radar/Metro.aspx">Metro</a></li>
								<li><a href="/National/Radar/Metro.aspx?animate=true">Metro Loop</a></li>
								<li><a href="/National/Radar/Summary.aspx">Summary</a></li>
								<li><a href="/Local/WxMap.aspx">Java Radar Loop</a></li>
							</ul>
						</li>
						<li><a href="/National/Satellite/Default.aspx" class="toggle-btn">Satellite</a>
							<ul>
								<li><a href="/Global/Satellite/Infrared.aspx">Global Infrared</a></li>
								<li><a href="/National/Satellite/Infrared.aspx">US National Infrared</a></li>
								<li><a href="/National/Satellite/Regional.aspx">US Regional Infrared</a></li>
								<li><a href="/Storm/Hurricane/AtlanticSatellite.aspx">Atlantic Infrared Satellite</a></li>
								<li><a href="/Storm/Hurricane/PacificSatellite.aspx">Pacific Infrared Satellite</a></li>
								<li><a href="/Storm/Hurricane/CaribbeanSatellite.aspx">Caribbean Infrared Satellite</a></li>
								<li><a href="/National/Satellite/Visible.aspx">Visible Satellite</a></li>
								<li><a href="/Global/Satellite/Current.aspx">Current Satellite</a></li>
								<li><a href="/National/Satellite/WaterVapor.aspx">Water Vapor</a></li>
							</ul>
						</li>
					</ul>
				</div>
				<div class="social">
					<div class="fb">
						<iframe class="fb-btn" src="http://www.facebook.com/plugins/like.php?href=http%3A%2F%2Fwww.facebook.com%2FIntellicast.weather&amp;send=false&amp;layout=button_count&amp;width=100&amp;show_faces=false&amp;action=like&amp;colorscheme=light&amp;font&=tahomaamp;height=21" scrolling="no" frameborder="0" allowTransparency="true"></iframe>
					</div>
				</div>
			</div>
		</div>
		<div class="location-bar clearfix">
			<div class="container">
				<div class="row">
					<div class="user-location">
						<a class="current-location toggle-btn" href="/Local/Weather.aspx?location=">
							 
						</a>
						
					</div>
					<div class="uni-time">
						<strong>Universal Time:</strong>&nbsp;Wednesday, 07 Jan 2015, 08:46
					</div>
				</div>
			</div>
		</div>
		<div id="referral" class="referral-form">
			<div id="referralForm" style="display:block;">
				<strong><span style="margin-right:5px;">Send this page to a friend</span><img src="http://images.intellicast.com/App_Images/btn_close.gif" alt="Close" class="Close" onclick="javascript:$('#referral').toggle();" style="float:right;" /></strong>
				<div>
					Your Name:<br />
					<input type="text" id="FromName" maxlength="50" />
				</div>
				<div>
					Your E-mail Address: <br />
					<input type="text" id="FromEmail" maxlength="256" />
				</div>
				<div>
					Friend's E-mail Address: <br />
					<input type="text" id="ToEmail" maxlength="256" />
				</div>
				<div>
					<button type="button" id="ReferralSubmit" onclick="javascript: SendReferral()">Send</button>
				</div>
			</div>
			<div id="referralMessage" style="display:none;">
				<strong style="text-align:center;">Thank you!<img src="http://images.intellicast.com/App_Images/btn_close.gif" alt="Close" class="Close" onclick="javascript:$('#referral').toggle();" /></strong>
				<br /><br />
					Your referral has been sent.<br /><br />
					<a href="#" onclick="javascript:$('#referralForm').show();$('#referralMessage').hide();">Click here to send another one.</a>
			</div>
		</div>
	</header>

	<div class="main container clearfix" id="content">
		<form name="aspnetForm" method="post" action="Influenza.aspx" id="aspnetForm">
<input type="hidden" name="__VIEWSTATE" id="__VIEWSTATE" value="/wEPDwUKMTE0MzE0NTAzOWRke0gtqXnI0wxsgWeOlP4f4KOKH48=" />

<input type="hidden" name="__VIEWSTATEGENERATOR" id="__VIEWSTATEGENERATOR" value="9A160A87" />
			
<div style="clear:both;width:1000px;">
<div id="leftColumn">
  
<div class="pageHeader">
  <div class="Primary Header" style="float: left;">Influenza</div>
  <div class="breadcrumb"><a href="/">Home</a> &raquo; <a href="/Health">Health</a> &raquo; Influenza</div>
</div>
<div class="Chrome RelatedTop">
  <div class="Title">Health Maps</div>
</div>
<div class="Container RelatedContainer">
  <table class="Related">
    <tr>
      <td>
        <a href="/Health/AchesPains.aspx"><img src="http://images.intellicast.com/WxImages/_100w/AchesPains/usa.jpg" alt="Aches &amp; Pains" class="Thumbnail" /><br />
        Aches &amp; Pains</a>
      </td>
      <td>
        <a href="/Health/Air.aspx"><img src="http://images.intellicast.com/WxImages/_100w/AirStagnation/usa.jpg" alt="Air Stagnation" class="Thumbnail" /><br />
        Air Stagnation</a>
      </td>
      <td>
        <a href="/Health/Attentiveness.aspx"><img src="http://images.intellicast.com/WxImages/_100w/Attentiveness/usa.jpg" alt="Attentiveness" class="Thumbnail" /><br />
        Attentiveness</a>
      </td>
      <td>
        <a href="/Health/BadHairDay.aspx"><img src="http://images.intellicast.com/WxImages/_100w/BadHairDay/usa.jpg" alt="Bad Hair Day" class="Thumbnail" /><br />
        Bad Hair Day</a>
      </td>
      <td class="Selected">
        <a href="/Health/Influenza.aspx"><img src="http://images.intellicast.com/WxImages/_100w/InfluenzaReport/usa.jpg" alt="Influenza Report" class="Thumbnail" /><br />
        Influenza Report</a>
      </td>
    </tr>
    <tr>
      <td>
        <a href="/Health/Labor.aspx"><img src="http://images.intellicast.com/WxImages/_100w/LaborIndex/usa.jpg" alt="Labor Index" class="Thumbnail" /><br />
        Labor Index</a>
      </td>
      <td>
        <a href="/Health/Mood.aspx"><img src="http://images.intellicast.com/WxImages/_100w/MoodIndex/usa.jpg" alt="Mood Index" class="Thumbnail" /><br />
        Mood Index</a>
      </td>
      <td>
        <a href="/Health/Reflex.aspx"><img src="http://images.intellicast.com/WxImages/_100w/ReflexTimes/usa.jpg" alt="Reflex Times" class="Thumbnail" /><br />
        Reflex Times</a>
      </td>
      <td>
        <a href="/Health/Respiratory.aspx"><img src="http://images.intellicast.com/WxImages/_100w/RespitoryDistress/usa.jpg" alt="Respiratory Distress" class="Thumbnail" /><br />
        Respiratory Distress</a>
      </td>
      <td>
        <div class="FocusIcon">
          <a href="/Local/Weather.aspx">
            <img src="http://images.intellicast.com/App_Images/thx_header.gif" alt="Image Header" />
            <img src="http://images.intellicast.com/App_Images/thx_weather.gif" alt="Local Weather"  />
            Local Weather
          </a>
        </div>
      </td>
    </tr>
  </table>    
</div> 

<div class="Chrome">
  <div class="Title">Influenza Report</div>
</div>
<div style="border-left:solid .5px #999;">
  <table id="controls">
    <tr style="vertical-align: top;">
      <td>
        
      </td>
      <td style="width:800px;padding: 10px 10px 0px 0px;">
        
      </td>
    </tr>
    <!-- Check if Singleton -->
    
  <tr>
    <td colspan="2" style="padding:0px 10px 10px 10px;">
		  
			<!-- Hurricane Irene Interact Button -->
			

      <div style="float:left;width:560px;margin-left: 10px;">
        <strong>Did you know?</strong><br />
        You can Animate, Pan &amp; Zoom many of our weather maps with the <a href="/Local/WxMap.aspx">Interactive Weather Map</a>.<br />
        View Radar, Satellite, Temperature, Snow Cover, Storms and more by zooming directly over your area.
      </div>
    </td>
  </tr>
  
  </table>
</div>
<div class="Content Container" style="text-align:center;">
  <div><img style="margin-bottom:-2px;" src="http://images.intellicast.com/images/legends/InfluenzaReport.gif" alt="Legend" /></div><div id="ctl00_ctl00_master_body_map_basemap" style="background-repeat: no-repeat; background-position: center;">
    <div id="ctl00_ctl00_master_body_map_weather" style="background-repeat: no-repeat; background-position: center;">
      <div id="ctl00_ctl00_master_body_map_counties" style="background-repeat: no-repeat; background-position: center;">
        <div id="ctl00_ctl00_master_body_map_highways" style="background-repeat: no-repeat; background-position: center;">
          <div id="ctl00_ctl00_master_body_map_watchboxes" style="background-repeat: no-repeat; background-position: center;">
            <div id="ctl00_ctl00_master_body_map_cities" style="background-repeat: no-repeat; background-position: center;">
              <img id="map" src="http://images.intellicast.com/WxImages/InfluenzaReport/usa.jpg" alt="" style="visibility: visible;" usemap="#singletonMap" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
<script type="Text/javascript">
function ToggleOverlay(checkbox, folder) {
  switch (folder) {
    case "counties":
      var control = document.getElementById("ctl00_ctl00_master_body_map_counties");
      break;
    case "highways":
      var control = document.getElementById("ctl00_ctl00_master_body_map_highways");
      break;
    case "cities":
      var control = document.getElementById("ctl00_ctl00_master_body_map_cities");
      break;
    case "WatchBoxes":
      var control = document.getElementById("ctl00_ctl00_master_body_map_watchboxes");
      break;
  }
  switch (folder) {
    case "WatchBoxes":
      control.style.backgroundImage = checkbox.checked ? 'url(http://images.intellicast.com/WxImages/SingletonWatchBoxes/usa.gif)' : '';
      break;
    default:
      control.style.backgroundImage = checkbox.checked ? 'url(http://images.intellicast.com/images/Overlays/Singleton/' + folder + '/usa.gif)' : '';
      break;
  }
  setCookie(folder + "Overlay", checkbox.checked, 365);
}

function LoadOverlays() {
  if (getCookie("countiesOverlay") != "") {
    var counties = document.getElementById("ctl00_ctl00_master_body_map_counties");
    var countiesToggle = document.getElementById("ToggleCounties");
    counties.style.backgroundImage = getCookie("countiesOverlay") == "true" ? 'url(http://images.intellicast.com/images/Overlays/Singleton/counties/usa.gif)' : '';
    countiesToggle.checked = (getCookie("countiesOverlay") == "true");
  }
  if (getCookie("highwaysOverlay") != "") {
    var highways = document.getElementById("ctl00_ctl00_master_body_map_highways");
    var highwaysToggle = document.getElementById("ToggleHighways");
    highways.style.backgroundImage = getCookie("highwaysOverlay") == "true" ? 'url(http://images.intellicast.com/images/Overlays/Singleton/highways/usa.gif)' : '';
    highwaysToggle.checked = (getCookie("highwaysOverlay") == "true");
  }
  if (getCookie("citiesOverlay") != "") {
    var cities = document.getElementById("ctl00_ctl00_master_body_map_cities");
    var citiesToggle = document.getElementById("ToggleCities");
    cities.style.backgroundImage = getCookie("citiesOverlay") == "true" ? 'url(http://images.intellicast.com/images/Overlays/Singleton/cities/usa.gif)' : '';
    citiesToggle.checked = (getCookie("citiesOverlay") == "true");
  }
  if (getCookie("WatchBoxesOverlay")) {
    var WatchBoxes = document.getElementById("ctl00_ctl00_master_body_map_watchboxes");
    var WatchBoxesToggle = document.getElementById("ToggleWatchboxes");
    WatchBoxes.style.backgroundImage = getCookie("WatchBoxesOverlay") == "true" ? 'url(http://images.intellicast.com/WxImages/SingletonWatchBoxes/usa.gif)' : body_map_watchboxes");
    var WatchBoxesToggle = document.getElementById("ToggleWatchboxes");
    WatchBoxes.style.backgroundImage = getCookie("WatchBoxesOverlay") == '';
    WatchBoxesToggle.checked = (getCookie("WatchBoxesOverlay") == "true");
  }
}
</script>

<div class="Chrome">
  <div class="Title">Learn about Influenza</div>
  <div style="float: right;"><a style="color:#FFF;font-family:'Segoe UI', Tahoma, sans-serif;" href="http://en.wikipedia.org/wiki/Influenza" target="_blank">View Detailed Information &raquo;</a></div>
</div>
<div id="weathercom" class="Content Container">
  The Influenza Report map shows areal coverage of the flu in each state for the current day.<br /><br />
  Influenza, commonly known as flu, is an infectious disease of birds and mammals caused by RNA viruses of the family Orthomyxoviridae (the influenza viruses). The name influenza comes from the Italian: influenza, meaning "influence", (Latin: influentia). In humans, common symptoms of the disease are the chills, then fever, sore throat, muscle pains, severe headache, coughing, weakness and general discomfort. In more serious cases, influenza causes pneumonia, which can be fatal, particularly in young children and the elderly. Although it is sometimes confused with the common cold, influenza is a much more severe disease and is caused by a different type of virus. Influenza can produce nausea and vomiting, especially in children, but these symptoms are more characteristic of the unrelated gastroenteritis, which is sometimes called "stomach flu" or "24-hour flu".<br /><br />
  Typically influenza is transmitted from infected mammals through the air by coughs or sneezes, creating aerosols containing the virus, and from infected birds through their droppings. Influenza can also be transmitted by saliva, nasal secretions, faeces and blood. Infections also occur through contact with these body fluids or with contaminated surfaces. Flu viruses can remain infectious for about one week at human body temperature, over 30 days at 0 °C (32 °F), and for much longer periods at very low temperatures. Most influenza strains can be inactivated easily by disinfectants and detergents.<br /><br />
  Flu spreads around the world in seasonal epidemics, killing millions of people in pandemic years and hundreds of thousands in non-pandemic years. Three influenza pandemics occurred in the 20th century and killed tens of millions of people, with each of these pandemics being caused by the appearance of a new strain of the virus in humans. Often, these new strains result from the spread of an existing flu virus to humans from other animal species. A deadly avian strain named H5N1 has posed the greatest risk for a new influenza pandemic since it first killed humans in Asia in the 1990s. Fortunately, this virus has not mutated to a form that spreads easily between pehumans from other animal species. A deadly avian strain named H5N1 has posed the greateople.<br /><br />
  Vaccinations against influenza are usually given to people in developed countries with a high risk of contracting the disease and to farmed poultry. The most common human vaccine is the trivalent influenza vaccine that contains purified and inactivated material from three viral strains. Typically, this vaccine includes material from two influenza A virus subtypes and one influenza B virus strain. A vaccine formulated for one year may be ineffective in the following year, since the influenza virus changes rapidly over time, and different strains become dominant. Antiviral drugs can be used to treat influenza, with neuraminidase inhibitors being particularly effective.
</div>   

</div>
<div id="rightColumn">
  <div class="adHeader">
    <div class="title">Advertisements</div>
    <div class="miniAd"><a href="http://www.aolnews.com/category/nation/" target="_blank" onclick="javascript:linkTracker._trackPageview('/AOL/Header/www.aolnews.com/category/nation/');"><span>National News</span></a></div>
  </div>
  <div id="promoAd">
    
    <img style="display:block;" src="http://images.intellicast.com/App_Images/mobile_app_ad_small_severe_weather.png" alt="Track Severe Weather on mobile" />
		<a style="position:absolute;display:block;width:94px;height:36px;top:78px;left:14px;" href="https://itunes.apple.com/us/app/intellicast-weather-radar/id600105016" target="_blank"></a>
		<a style="position:absolute;display:block;width:75px;height:36px;top:78px;left:111px;" href="https://itunes.apple.com/us/app/intellicast-hd-weather-radar/id408451987" target="_blank"></a>
		<a style="position:absolute;display:block;width:99px;height:36px;top:78px;left:189px;" href="https://play.google.com/store/apps/details?id=com.wsi.android.intellicast" target="_blank"></a>
	</div>
  <!-- Old Ads -->
  
  <!-- Old Ads -->
  <!-- New Ads -->
  <div id="AdPageCounterDiv">
    <div id="WX_PageCounter">
      <script type="text/javascript">A21.notifyRegistered("WX_PageCounter");</script>
    </div>
  </div>
  <div id="WX_Top300Variable">
    <script type="text/javascript">A21.notifyRegistered("WX_Top300Variable");</script>
  </div>
  
  <div id="WX_Mid300">
    <script type="text/javascript">      A21.notifyRegistered("WX_Mid300");</script>
  </div>
  <div id="WX_Hidden">
    <script type="text/javascript">      A21.notifyRegistered("WX_Hidden");</script>
  </div>
  <div id="WX_Tiles">
		<div style="float:left;">
      <div id="WX_Tile1">
        <script type="text/javascript">          A21.notifyRegistered("WX_Tile1");</script>
      </div>
		</div>
		<div style="float:right;">
      <div id="WX_Tile2">
        <script type="text/javascript">          A21.notifyRegistered("WX_Tile2");</script>
      </div>
		</div>
	</div>
  <div id="WX_PaidSearch">
    <script type="text/javascript">        A21.notifyRegistered("WX_PaidSearch");</script>
  </div>
  <!-- New Ads -->
</div>
</div>

		</form>
	</div>

	
		<footer class="site-footer">
			<div class="help-links">
				<div class="container">
					<a href="/About/Default.aspx">About Us</a>
					<a href="http://twcmediakit.com/contact/" target="_blank">Advertise with Us</a>
					<a href="/About/Contact.aspx">Contact Us</a>
					<a href="/Members/Default.aspx">Account</a>
					<a href="/Help/Default.aspx">Help</a>
					<a href="http://www.aolnews.com" target="_blank">News</a>
					<a href="http://www.aboutads.info/choices/" target="_blank" style="background: url(http://icons.wxug.com/i/misc/adChoices.png) no-repeat right; padding-right: 20px;">Ad Choices</a>
				</div>
			</div>
			<div class="legal-links">
				<div class="container">
					<a href="/About/Terms.aspx">Terms of Use</a>
					<a href="/About/Privacy.aspx">Privacy Statement</a>
					<span>&copy; 2015</span>
					<span>Partnered with <a href="http://www.wunderground.com" target="_blank">Weather Underground</a></span>
					<span>Version 4.6.201311.1</span>
				</div>
				<!-- MACHINE NAME: ICAST-WEB1 -->
			</div>
		</footer>
	
	<script type="text/javascript">
		var pageTracker = _gat._getTracker("UA-3434520-1");
		pageTracker._initData();
		pageTracker._trackPageview();
	</script>
	
	<script type="text/javascript" src="http://tags.crwdcntrl.net/c/2217/cc.js?ns=_cc2217" id="LOTCC_2217"></script>
	<script type="text/javascript" src="http://images.intellicast.com/Scripts/cc_wrapper_20130215.js"></script>
	<script type="text/javascript" src="http://images.intellicast.com/Scripts/core_20140106.js"></script>
  
  <!-- Begin Omniture Tag -->
  <script type="text/javascript">
    (function () { 
      var _recentLocations = [];
      
      _recentLocations.push(1);
      
      _recentLocations = _recentLocations.length ? _recentLocations.join() : "";

      var _brdcrmb = window.location.pathname.length !== "/" ? window.location.pathname.substring(1).split("/") : ["Home"];
      if (_brdcrmb.length > 1) {
        _brdcrmb.unshift("Home");
        _brdcrmb[_brdcrmb.length - 1] = _brdcrmb[_brdcrmb.length - 1].substring(0, _brdcrmb[_brdcrmb.length - 1].indexOf("."));
      }
      s.pageName = document.title;
      s.prop2 = s.evar4 = _brdcrmb.join(" > "); // sub section e.g. home > maps > regionmap
      s.prop6 = s.evar6 = "::"; // city:state:country
      s.prop8 = s.evar8 = ""; // dma id
      s.prop9 = s.evar9 = ""; // zip code    
      s.evar23 = ""; // marketing campaigns
      s.evar24 = window.location.href; // full url
      //SiteCatalyst Beacon Parameter: event15 Page View Event
    
      s.prop10 = _recentLocations; // recent locations, number then location id
      s.prop14 = ""; // intra-page actions
      s.evar10 = _recentLocations; // saved locations, number then location id

      s.evar11 = ""; // internal search term
      s.evar12 = ""; // member type (member, non-member)
      s.evar13 = ""; // weather map layer
      s.evar14 = ""; // boating map layer
      s.evar17 = ""; // search type (other or zip)
      s.evar18 = ""; // search error message
      /*
        event7: Membership Signup
        event8: Sign-Ins
        event9: Sign-Outs
      */
      var s_code = s.t();
      if (s_code) document.write(s_code);
    }());
  </script>
  <!-- End Omniture Tag -->
</body>
</html>
sage
      /*
        event7: Members