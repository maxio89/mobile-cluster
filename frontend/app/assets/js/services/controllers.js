/**
 * Services controllers.
 */
define(['underscore'], function () {
    'use strict';

    var ServicesCtrl = function ($scope, playRoutes) {

    };
    ServicesCtrl.$inject = ['$scope', 'playRoutes'];

    var RastriginCtrl = function ($scope, $interval, $http, playRoutes) {
        var frontendWebsocketUrl = playRoutes.controllers.services.ga.Rastrigin.frontendWebsocket().webSocketUrl();
        var frontendWs = new WebSocket(frontendWebsocketUrl);

        $scope.dimension = 10;
        $scope.initialSize = 100;
        $scope.maxSize = 100;
        $scope.mu = 0.8;
        $scope.xover = 0.8;
        $scope.maxCycles = 10000;
        $scope.snapshotFreq = 1000;
        $scope.migrationFreq = 1000;
        $scope.migrationFactor = 0;
        $scope.leavePopulation = false;
        $scope.results = {};

        //TODO check if in case of starting another work, previous work results are cleared
        frontendWs.onmessage = function (msg) {
            var data = JSON.parse(msg.data);
            var result = $scope.results[data.hostname];
            if (!angular.isUndefined(result)) {
                result = result[data.workerId];
                if (angular.isUndefined(result)) {
                    result = {};
                    result.data = [{values: [], key: 'Rastrigin function'}];
                    $scope.results[data.hostname][data.workerId] = result;
                } else {
                    if (!angular.isUndefined(result.finished)) {
                        result.finished = undefined;
                        result.data[0].values = [];
                    }
                }
            } else {
                $scope.results[data.hostname] = {};
                result = {};
                result.data = [{values: [], key: 'Rastrigin function'}];
                $scope.results[data.hostname][data.workerId] = result;
            }
            result.value = data.value;
            result.point = data.point;
            result.cycles = data.cycles;
            result.data[0].values.push({x: result.cycles, y: result.value});
            result.runtime = data.runtime / 1000;
            //if (result.cycles === $scope.maxCycles || result.value < 0.1) {
            if (result.cycles === $scope.maxCycles) {
                result.finished = new Date();
                console.log("Worker id: " + data.workerId);
                console.log("Runtime: " + result.runtime);
                console.log("Point:");
                angular.forEach(result.point, function(val) {
                    console.log(val);
                });
                console.log("Cycles:");
                angular.forEach(result.data[0].values, function(val) {
                    console.log(val.x);
                });
                console.log("Values:");
                angular.forEach(result.data[0].values, function(val) {
                    console.log(val.y);
                });
            }
        };


        /**
         * Starting n-cycles reproduction
         */
        $scope.run = function () {
            frontendWs.send(JSON.stringify({
                n: $scope.dimension,
                initialSize: $scope.initialSize,
                maxSize: $scope.maxSize,
                xover: $scope.xover,
                mu: $scope.mu,
                maxCycles: $scope.maxCycles,
                snapshotFreq: $scope.snapshotFreq,
                migrationFreq: $scope.migrationFreq,
                migrationFactor: $scope.migrationFactor,
                leavePopulation: $scope.leavePopulation
            }));
        };

        $scope.options = {
            chart: {
                type: 'lineWithFocusChart',
                height: 400,
                focusHeight: 40,
                margin: {
                    top: 20,
                    right: 20,
                    bottom: 20,
                    left: 60
                },
                x: function (d) {
                    return d.x;
                },
                y: function (d) {
                    return d.y;
                },
                useInteractiveGuideline: true,
                transitionDuration: 1000,
                //yScale:  d3.scale.log(),
                yAxis: {
                    tickFormat: function (d) {
                        return d3.format('.6f')(d);
                    }
                },
                y2Axis: {
                    tickFormat: function (d) {
                        return d3.format('.6f')(d);
                    }
                },
                tooltips: true,
                tooltipContent: function (key, x, y, e, graph) {
                    return '<h3>' + key + '</h3>' +
                        '<p>' + y + ' at ' + x + '</p>';
                }
            }
        };

        $scope.loadHistoricalData = function () {
            $http.get('/api/services/historicalData')
                .success(function (data) {
                    console.log(data);
                })
                .error(function (data) {
                    console.log(data);
                });
        };

        /**
         * Just apply model...
         */
        $interval(function () {
        }, 1000, 0, true);
    };
    RastriginCtrl.$inject = ['$scope', '$interval', '$http', 'playRoutes'];

    return {
        RastriginCtrl: RastriginCtrl
    };

});
