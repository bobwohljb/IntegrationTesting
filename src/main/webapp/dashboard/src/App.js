import React, { useState, useEffect } from 'react';
import Papa from 'papaparse';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import './App.css';

function App() {
  const [metricsData, setMetricsData] = useState([]);
  const [selectedTest, setSelectedTest] = useState('all');
  const [chartMetric, setChartMetric] = useState('ExecutionTimeMs');
  const [availableMetrics, setAvailableMetrics] = useState([]);

  useEffect(() => {
    // Load the CSV file
    fetch('http://localhost:3000/metrics.csv')
      .then(response => response.text())
      .then(csvText => {
        Papa.parse(csvText, {
          header: true,
          complete: (results) => {
            setMetricsData(results.data.filter(row => row.TestName));

            // Extract available metrics from the first row
            if (results.data.length > 0) {
              const metrics = Object.keys(results.data[0])
                .filter(key => key !== 'TestName' && key !== 'TestStartTime' && key !== 'TestEndTime');
              setAvailableMetrics(metrics);
            }
          }
        });
      })
      .catch(error => console.error('Error loading metrics data:', error));
  }, []);

  // Filter data based on selected test
  const filteredData = selectedTest === 'all' 
    ? metricsData 
    : metricsData.filter(row => row.TestName === selectedTest);

  // Prepare data for the chart
  const chartData = filteredData.map(row => ({
    name: row.TestName,
    value: parseFloat(row[chartMetric]) || 0
  }));

  return (
    <div className="App">
      <header className="App-header">
        <h1>Metrics Dashboard</h1>
      </header>

      <div className="dashboard-container">
        <div className="sidebar">
          <div className="test-selector">
            <h2>Tests</h2>
            <div 
              className={`test-item ${selectedTest === 'all' ? 'selected' : ''}`}
              onClick={() => setSelectedTest('all')}
            >
              All Tests
            </div>
            {metricsData.map(row => (
              <div 
                key={row.TestName}
                className={`test-item ${selectedTest === row.TestName ? 'selected' : ''}`}
                onClick={() => setSelectedTest(row.TestName)}
              >
                {row.TestName}
              </div>
            ))}
          </div>

          <div className="metric-selector">
            <h2>Metrics</h2>
            {availableMetrics.map(metric => (
              <div 
                key={metric}
                className={`metric-item ${chartMetric === metric ? 'selected' : ''}`}
                onClick={() => setChartMetric(metric)}
              >
                {metric}
              </div>
            ))}
          </div>
        </div>

        <div className="main-content">
          <div className="chart-container">
            <h2>{chartMetric} by Test</h2>
            <ResponsiveContainer width="95%" height={400}>
              <BarChart data={chartData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="value" fill="#8884d8" name={chartMetric} />
              </BarChart>
            </ResponsiveContainer>
          </div>

          <div className="table-container">
            <h2>Metrics Table</h2>
            <table className="metrics-table">
              <thead>
                <tr>
                  <th>Test Name</th>
                  {availableMetrics.map(metric => (
                    <th key={metric}>{metric}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {filteredData.map(row => (
                  <tr 
                    key={row.TestName}
                    className={selectedTest === row.TestName ? 'selected-row' : ''}
                    onClick={() => setSelectedTest(row.TestName)}
                  >
                    <td>{row.TestName}</td>
                    {availableMetrics.map(metric => (
                      <td key={metric}>{row[metric]}</td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;
