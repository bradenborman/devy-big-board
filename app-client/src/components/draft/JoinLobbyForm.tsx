import React, { useState } from 'react';
import './join-lobby-form.scss';

interface JoinLobbyFormProps {
  availablePositions: string[];
  onJoin: (nickname: string, position: string) => Promise<void>;
}

const JoinLobbyForm: React.FC<JoinLobbyFormProps> = ({ availablePositions, onJoin }) => {
  const [nickname, setNickname] = useState('');
  const [position, setPosition] = useState('');
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!nickname.trim()) {
      newErrors.nickname = 'Nickname is required';
    } else if (nickname.length < 2) {
      newErrors.nickname = 'Nickname must be at least 2 characters';
    } else if (nickname.length > 20) {
      newErrors.nickname = 'Nickname must be 20 characters or less';
    }

    if (!position) {
      newErrors.position = 'Please select a position';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setLoading(true);
    setErrors({});

    try {
      await onJoin(nickname.trim(), position);
    } catch (error) {
      console.error('Error joining lobby:', error);
      setErrors({
        submit: error instanceof Error ? error.message : 'Failed to join lobby. Please try again.',
      });
    } finally {
      setLoading(false);
    }
  };

  const handleNicknameChange = (value: string) => {
    setNickname(value);
    if (errors.nickname) {
      setErrors((prev) => {
        const newErrors = { ...prev };
        delete newErrors.nickname;
        return newErrors;
      });
    }
  };

  const handlePositionChange = (value: string) => {
    setPosition(value);
    if (errors.position) {
      setErrors((prev) => {
        const newErrors = { ...prev };
        delete newErrors.position;
        return newErrors;
      });
    }
  };

  return (
    <div className="join-lobby-form">
      <h2>Join the Draft</h2>
      <p className="form-description">Choose your position and enter your nickname to join</p>

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="nickname">
            Nickname <span className="required">*</span>
          </label>
          <input
            id="nickname"
            type="text"
            value={nickname}
            onChange={(e) => handleNicknameChange(e.target.value)}
            placeholder="Enter your nickname"
            className={errors.nickname ? 'error' : ''}
            disabled={loading}
            maxLength={20}
          />
          {errors.nickname && <span className="error-message">{errors.nickname}</span>}
        </div>

        <div className="form-group">
          <label htmlFor="position">
            Position <span className="required">*</span>
          </label>
          <select
            id="position"
            value={position}
            onChange={(e) => handlePositionChange(e.target.value)}
            className={errors.position ? 'error' : ''}
            disabled={loading || availablePositions.length === 0}
          >
            <option value="">Select a position</option>
            {availablePositions.map((pos) => (
              <option key={pos} value={pos}>
                Position {pos}
              </option>
            ))}
          </select>
          {errors.position && <span className="error-message">{errors.position}</span>}
          {availablePositions.length === 0 && (
            <span className="info-message">No positions available</span>
          )}
        </div>

        {errors.submit && (
          <div className="error-banner">
            <span className="error-icon">⚠️</span>
            <span>{errors.submit}</span>
          </div>
        )}

        <button type="submit" className="btn btn-primary" disabled={loading || availablePositions.length === 0}>
          {loading ? (
            <>
              <span className="spinner"></span>
              Joining...
            </>
          ) : (
            'Join Lobby'
          )}
        </button>
      </form>
    </div>
  );
};

export default JoinLobbyForm;
