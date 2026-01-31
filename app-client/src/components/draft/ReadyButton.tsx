import React from 'react';
import './ready-button.scss';

interface ReadyButtonProps {
  isReady: boolean;
  onToggle: () => void;
  disabled?: boolean;
}

const ReadyButton: React.FC<ReadyButtonProps> = ({ isReady, onToggle, disabled = false }) => {
  return (
    <button
      className={`ready-button ${isReady ? 'ready' : 'not-ready'}`}
      onClick={onToggle}
      disabled={disabled}
    >
      {isReady ? (
        <>
          <span className="icon">✓</span>
          <span>Ready</span>
        </>
      ) : (
        <>
          <span className="icon">○</span>
          <span>Not Ready</span>
        </>
      )}
    </button>
  );
};

export default ReadyButton;
