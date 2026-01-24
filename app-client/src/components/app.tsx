import React from 'react';
import './app.scss';
import AppWrapper from './appWrapper';

const App: React.FC = () => {
  if (
    window.location.protocol === 'http:' &&
    window.location.hostname !== 'localhost'
  ) {
    window.location.href = window.location.href.replace('http:', 'https:');
    return null;
  }

  return (
    <div className="app">
      <AppWrapper />
    </div>
  );
};

export default App;
