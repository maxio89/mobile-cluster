/**
 * Services controllers.
 */
define(['underscore'], function () {
    'use strict';

    var ServicesCtrl = function ($scope, playRoutes) {

    };
    ServicesCtrl.$inject = ['$scope', 'playRoutes'];

    var RastriginCtrl = function ($scope, $interval, playRoutes) {
        var frontendWebsocketUrl = playRoutes.controllers.services.ga.Rastrigin.frontendWebsocket().webSocketUrl();
        var frontendWs = new WebSocket(frontendWebsocketUrl);

        $scope.dimension = 2;
        $scope.initialSize = 100;
        $scope.maxSize = 150;
        $scope.mu = 0.4;
        $scope.xover = 0.8;
        $scope.maxCycles = 100;
        $scope.snapshotFreq = 100;
        $scope.results = {};

        frontendWs.onmessage = function (msg) {

            var data = JSON.parse(msg.data);
            //$scope.$apply(function () {
                var result = $scope.results[data.hostname];
                if (!angular.isUndefined(result)) {
                    result = result[data.workerId];
                }
                if (angular.isUndefined(result)) {
                    result = {};
                    result.data = [{values: [], key: 'Rastrigin function'}];
                    console.log("aaa");
                }
                $scope.results[data.hostname] = {};
                $scope.results[data.hostname][data.workerId] = result;
                result.value = data.value;
                result.point = data.point;
                result.cycles = data.cycles;
                if (result.cycles === $scope.maxCycles) {
                    result.finished = new Date();
                    console.log(result);
                }
                result.data[0].values.push({x: result.cycles, y: result.value});
                result.runtime = data.runtime / 1000;
            //});
        };


        /**
         * Starting n-cycles reproduction
         */
        $scope.run = function () {
            $scope.done = null;
            $scope.results = {};

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



        //$scope.options.chart.yScale = d3.scale.log();
        //$scope.options.chart.yAxis.tickValues = [1, 10, 100, 1000, 10000, 1000000];
        //$scope.options.chart.forceY = [1, 1000000];
        /**
         * Just apply model...
         */
        $interval(function () {
        }, 1000, 0, true);
    };
    RastriginCtrl.$inject = ['$scope', '$interval', 'playRoutes'];


    return {
        RastriginCtrl: RastriginCtrl
    };

});
