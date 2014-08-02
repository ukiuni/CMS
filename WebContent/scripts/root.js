function findLanguage() {
	try {
		return (navigator.browserLanguage || navigator.language || navigator.userLanguage).substr(0, 2)
	} catch (e) {
		return "en";
	}
}
var myApp = angular.module('myApp', [ 'ui.bootstrap', 'hc.marked', 'ngRoute', 'ngCookies', 'angularFileUpload', 'pascalprecht.translate' ]);
myApp.config([ 'markedProvider', '$routeProvider', '$locationProvider', '$translateProvider', function(markedProvider, $routeProvider, $locationProvider, $translateProvider) {
	markedProvider.setOptions({
		gfm : true,
		tables : true,
		breaks : true,
		highlight : function(code) {
			return hljs.highlightAuto(code).value;
		}
	});
	$translateProvider.useStaticFilesLoader({
		prefix : 'scripts/translate/lang_',
		suffix : '.json?' + new Date().getTime()
	});
	$translateProvider.preferredLanguage(findLanguage());
	$translateProvider.fallbackLanguage('en');
	$translateProvider.useMissingTranslationHandlerLog();
/*
	$routeProvider.when('/', {
		templateUrl : 'template/index.html'
	}).when('/myPage', {
		templateUrl : 'template/myPage.html?' + new Date().getTime()
	}).when('/editReport', {
		templateUrl : 'template/editReport.html?' + new Date().getTime()
	}).when('/report', {
		templateUrl : 'template/report.html?' + new Date().getTime()
	}).when('/editProfile', {
		templateUrl : 'template/editProfile.html?' + new Date().getTime()
	}).when('/login', {
		templateUrl : 'template/login.html?' + new Date().getTime()
	}).when('/license', {
		templateUrl : 'template/license.html?' + new Date().getTime()
	}).when('/logout', {
		templateUrl : 'template/logout.html'
	}).otherwise({
		redirectTo : '/'
	});
*/
	//load route from dynamic-generated script "setupRouteFunciton.js";
	setupRoute($routeProvider);
	$locationProvider.html5Mode(true);
} ]);
myApp.run([ "$rootScope", "$http", "$cookies", "$location", "$timeout", function($rootScope, $http, $cookies, $location, $timeout) {
	$rootScope.closeAlert = function() {
		$rootScope.alert = null;
	}
	$rootScope.cancelAlertCloseTimer = function() {
		if ($rootScope.timeoutHandle) {
			$timeout.cancel($rootScope.timeoutHandle);
		}
	}
	$rootScope.showAlert = function(message, type, time) {
		var useType = type ? type : 'danger';
		var useTime = time ? time : 5000;
		$rootScope.alert = {
			type : useType,
			message : message
		}
		$rootScope.timeoutHandle = $timeout(function() {
			$rootScope.closeAlert()
		}, useTime);
	}
	var accountAccessKey = $cookies.accountAccessKey;
	$rootScope.logout = function() {
		$location.path('/logout');
	}
	$rootScope.clone = function(obj) {
		if (null == obj || "object" != typeof obj)
			return obj;
		var copy = obj.constructor();
		for ( var attr in obj) {
			if (obj.hasOwnProperty(attr))
				copy[attr] = obj[attr];
		}
		return copy;
	}
	if (accountAccessKey) {
		$http.get("api/account/loadByAccessKey", {
			params : {
				accessKey : accountAccessKey
			}
		}).success(function(account) {
			$rootScope.loginAccount = account;
			if ("/" == $location.path()) {
				$location.path('/myPage');
			}
		}).error(function(e) {
			$rootScope.logout();
		});
	}
} ]);