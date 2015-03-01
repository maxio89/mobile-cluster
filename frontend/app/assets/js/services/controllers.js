/**
 * Services controllers.
 */
define(['underscore'], function () {
    'use strict';

    var ServicesCtrl = function ($scope, playRoutes) {

    };
    ServicesCtrl.$inject = ['$scope', 'playRoutes'];

    var RastriginCtrl = function ($scope, playRoutes) {
        var frontendWebsocketUrl = playRoutes.controllers.services.Rastrigin.frontendWebsocket().webSocketUrl();
        var frontendWs = new WebSocket(frontendWebsocketUrl);

        $scope.dimension = 2;
        $scope.initialSize = 100;
        $scope.maxSize = 150;
        $scope.mu = 0.4;
        $scope.xover = 0.8;
        $scope.maxCycles = 100;
        $scope.snapshotFreq = 10;

        frontendWs.onmessage = function (msg) {
            var data = JSON.parse(msg.data);
            $scope.$apply(function () {
                $scope.value = data.value;
                $scope.point = data.point;
                $scope.cycles = data.cycles;
                var currentDate = new Date();
                if (data.cycles === $scope.maxCycles) {
                    $scope.finished = currentDate;
                }
                if (!angular.isUndefined($scope.start)) {
                    $scope.runtime = (currentDate.getTime() - $scope.start.getTime()) / 1000;
                }
            });
        };


        /**
         * Starting n-cycles reproduction
         */
        $scope.run = function () {
            $scope.done = null;
            $scope.result = null;
            $scope.runtime = null;
            $scope.start = new Date();

            frontendWs.send(JSON.stringify({
                n: $scope.dimension,
                initialSize: $scope.initialSize,
                maxSize: $scope.maxSize,
                xover: $scope.xover,
                mu: $scope.mu,
                maxCycles: $scope.maxCycles,
                snapshotFreq: $scope.snapshotFreq
            }));
        };

    };
    RastriginCtrl.$inject = ['$scope', 'playRoutes'];


    return {
        RastriginCtrl: RastriginCtrl
    };

});
