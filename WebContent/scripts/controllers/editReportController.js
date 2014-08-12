myApp.controller("editReportController", [ "$rootScope", "$scope", "$http", "$location", "$modal", "$upload", function($rootScope, $scope, $http, $location, $modal, $upload) {
	if (!$rootScope.loginAccount) {
		$location.path('/login');
		return;
	}
	$scope.escapable = false;
	var removeLocationChangeStart = $scope.$on('$locationChangeStart', function(event, next, current) {
		if (!$scope.escapable && !confirm("Are you sure you want to leave this page?")) {
			event.preventDefault();
		} else {
			removeLocationChangeStart();
		}
	});
	var loadSuccess = function(data) {
		$scope.report = data;
		if ("published" == $scope.report.status) {
			$scope.action = "publish";
		} else if ("private" == $scope.report.status) {
			$scope.action = "save as private";
		} else {
			$scope.action = "save as draft";
		}
	};
	var loadFail = function(e) {
		$rootScope.showAlert($translate.instant('error.loadReport'));
		$location.path('/');
	};
	if ($location.search()["key"]) {
		$http.get("api/report/load/" + $location.search()["key"], {
			params : {
				accountAccessKey : $scope.loginAccount.accessKey,
			}
		}).success(loadSuccess).error(loadFail);
	} else {
		$http.get("api/report/create", {
			params : {
				accessKey : $scope.loginAccount.accessKey
			}
		}).success(loadSuccess).error(loadFail);
	}
	$scope.doAction = function() {
		$scope.escapable = true;
		if ("delete" != $scope.action) {
			$scope.report.accountAccessKey = $rootScope.loginAccount.accessKey;
			$http.put("api/report", $scope.report).success(function(data) {
				$location.search("lastAction", "{\"action\":\"saved\",\"key\":\"" + $scope.report.key + "\"}").path('/myPage');
			}).error(function(e) {
				$rootScope.showAlert($translate.instant('error.saveReport'));
			});
		} else {
			var ModalInstanceCtrl = [ "$scope", "$modalInstance", "title", "body", "report", function($scope, $modalInstance, title, body, report) {
				$scope.title = title;
				$scope.body = body;
				$scope.execute = function() {
					$modalInstance.close(report.key);
				};
			} ];
			var modalInstance = $modal.open({
				templateUrl : 'template/confirmDialog.html?' + new Date().getTime(),
				controller : ModalInstanceCtrl,
				resolve : {
					title : function() {
						return "Delete Report?";
					},
					body : function() {
						return "You realy want to delete this report?";
					},
					report : function() {
						return $scope.report;
					}
				}
			});
			modalInstance.result.then(function(deleteReportKey) {
				$scope.deleteReport(deleteReportKey);
			}, function() {
			});
		}
	};
	$scope.changeAction = function(action) {
		if ("publish" == action) {
			$scope.report.status = "published";
		} else if ("save as draft" == action) {
			$scope.report.status = "draft";
		} else if ("save as private" == action) {
			$scope.report.status = "private";
		}
		$scope.action = action;
	};
	$scope.cancel = function() {
		$scope.escapable = true;
		for ( var i in $location.search()) {
			$location.search(i, null)
		}
		$location.path('/myPage');
	};
	$scope.deleteReport = function(reportKey) {
		$scope.escapable = true;
		$http["delete"]("api/report", {
			params : {
				key : reportKey,
				accountAccessKey : $rootScope.loginAccount.accessKey
			}
		}).success(function() {
			$location.search("lastAction", "{\"action\":\"deleted\",\"key\":\"" + $scope.report.key + "\"}").path('/myPage');
		}).error(function() {
			$rootScope.showAlert($translate.instant('error.deleteReport'));
		});
	};
	$scope.onImageSelected = function($file) {
		(function() {
			var value = $upload.upload({
				url : "api/report/registImage",
				method : "POST",
				data : {
					accountAccessKey : $rootScope.loginAccount.accessKey,
					reportKey : $scope.report.key
				},
				file : $file
			}).progress(function(evt) {
				//console.log('percent: ' + parseInt(100.0 * evt.loaded / evt.total));
			}).success(function(imageKey, status, headers, config) {
				var imageUrl = "![](api/report/image/" + imageKey + ")";
				var insertIndex = document.getElementById('reportTextArea').selectionStart;
				if (insertIndex) {
					$scope.report.content = $scope.report.content.substring(0, insertIndex) + imageUrl + $scope.report.content.substring(insertIndex);
				} else {
					$scope.report.content = $scope.report.content + "\n" + imageUrl;
				}
				document.getElementById('imageInput').value = null;
			}).error(function(data, status, headers, config) {
				$rootScope.showAlert($translate.instant('error.upload'));
			});
		})();
	}
} ]);