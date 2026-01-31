import React, { useState } from 'react';
import './start-draft-button.scss';

interface StartDraftButtonProps {
  isCreator: boolean;
  allReady: boolean;
  onStart: () => void;
  loading?: boolean;
}

const StartDraftButton: React.FC<StartDraftButtonProps> = ({
  isCreator,
  allReady,
  onStart,
  loading = false,
}) => {
  const [showTooltip, setShowTooltip] = useState(false);

  if (!isCreator) {
    return null;
  }

  const canStart = !loading;
  const tooltipMessage = !allReady ? 'Not all participants are ready. You can still start and use force-pick for empty slots.' : '';

  return (
    <div className="start-draft-button-container">
      <button
        className="start-draft-button"
        onClick={onStart}
        disabled={!canStart}
        onMouseEnter={() => setShowTooltip(true)}
        onMouseLeave={() => setShowTooltip(false)}
      >
        {loading ? (
          <>
            <span className="spinner"></span>
            Starting Draft...
          </>
        ) : (
          <>
            <span className="icon">🚀</span>
            Start Draft
          </>
        )}
      </button>
      {showTooltip && tooltipMessage && (
        <div className="tooltip">{tooltipMessage}</div>
      )}
    </div>
  );
};

export default StartDraftButton;
