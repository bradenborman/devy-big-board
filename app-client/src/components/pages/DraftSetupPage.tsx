import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './draft-setup.scss';

interface CreateDraftRequest {
  draftName: string;
  creatorNickname: string;
  participantCount: number;
  totalRounds: number;
  pin: string;
  isSnakeDraft: boolean;
}

interface CreateDraftResponse {
  uuid: string;
  draftName: string;
  status: string;
  participantCount: number;
  totalRounds: number;
  pin: string;
}

// Generate random 4-digit PIN
const generateRandomPin = (): string => {
  return Math.floor(1000 + Math.random() * 9000).toString();
};

const DraftSetupPage: React.FC = () => {
  const navigate = useNavigate();
  const [currentStep, setCurrentStep] = useState(1);
  const [formData, setFormData] = useState<CreateDraftRequest>({
    draftName: '',
    creatorNickname: '',
    participantCount: 8,
    totalRounds: 3,
    pin: generateRandomPin(),
    isSnakeDraft: false,
  });
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const totalSteps = 3;

  const validateStep = (step: number): boolean => {
    const newErrors: Record<string, string> = {};

    if (step === 1) {
      if (!formData.draftName.trim()) {
        newErrors.draftName = 'Draft name is required';
      } else if (formData.draftName.length < 3) {
        newErrors.draftName = 'Draft name must be at least 3 characters';
      }

      if (!formData.creatorNickname.trim()) {
        newErrors.creatorNickname = 'Nickname is required';
      } else if (formData.creatorNickname.length < 2) {
        newErrors.creatorNickname = 'Nickname must be at least 2 characters';
      } else if (formData.creatorNickname.length > 20) {
        newErrors.creatorNickname = 'Nickname must be 20 characters or less';
      }
    }

    if (step === 3) {
      if (!formData.pin || !/^\d{4}$/.test(formData.pin)) {
        newErrors.pin = 'PIN must be exactly 4 digits';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleNext = () => {
    if (validateStep(currentStep)) {
      setCurrentStep((prev) => Math.min(prev + 1, totalSteps));
    }
  };

  const handleBack = () => {
    setCurrentStep((prev) => Math.max(prev - 1, 1));
    setErrors({});
  };

  const handleSubmit = async () => {
    if (!validateStep(currentStep)) {
      return;
    }

    setLoading(true);
    setErrors({});

    try {
      const response = await fetch('/api/live-drafts', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Failed to create draft' }));
        throw new Error(errorData.message || 'Failed to create draft');
      }

      const data: CreateDraftResponse = await response.json();
      
      navigate(`/draft/${data.uuid}/lobby`, {
        state: { 
          creatorNickname: formData.creatorNickname,
          pin: data.pin
        }
      });
    } catch (error) {
      console.error('Error creating draft:', error);
      setErrors({
        submit: error instanceof Error ? error.message : 'Failed to create draft. Please try again.',
      });
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (field: keyof CreateDraftRequest, value: string | number | boolean) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }));
    if (errors[field]) {
      setErrors((prev) => {
        const newErrors = { ...prev };
        delete newErrors[field];
        return newErrors;
      });
    }
  };

  return (
    <div className="draft-setup-page">
      <nav className="navbar">
        <div className="nav-content">
          <div className="logo" onClick={() => navigate('/')}>
            <span className="logo-icon">🏈</span>
            <span className="logo-text">Devy BigBoard</span>
          </div>
          <button onClick={() => navigate('/live-draft')} className="back-btn">
            ← Back to Home
          </button>
        </div>
      </nav>

      <div className="setup-content">
        <div className="setup-header">
          <h1>Create Live Draft</h1>
          <p>Step {currentStep} of {totalSteps}</p>
        </div>

        <div className="wizard-progress">
          {Array.from({ length: totalSteps }, (_, i) => i + 1).map((step) => (
            <div
              key={step}
              className={`progress-step ${step === currentStep ? 'active' : ''} ${step < currentStep ? 'completed' : ''}`}
            >
              <div className="step-circle">{step < currentStep ? '✓' : step}</div>
              <div className="step-label">
                {step === 1 && 'Basic Info'}
                {step === 2 && 'Settings'}
                {step === 3 && 'Security'}
              </div>
            </div>
          ))}
        </div>

        <div className="wizard-content">
          {currentStep === 1 && (
            <div className="wizard-step">
              <h2>Let's start with the basics</h2>
              <div className="form-group">
                <label htmlFor="draftName">
                  Draft Name <span className="required">*</span>
                </label>
                <input
                  id="draftName"
                  type="text"
                  value={formData.draftName}
                  onChange={(e) => handleInputChange('draftName', e.target.value)}
                  placeholder="e.g., 2025 Dynasty Rookie Draft"
                  className={errors.draftName ? 'error' : ''}
                  maxLength={100}
                  autoComplete="off"
                  autoFocus
                />
                {errors.draftName && <span className="error-message">{errors.draftName}</span>}
              </div>

              <div className="form-group">
                <label htmlFor="creatorNickname">
                  Your Nickname <span className="required">*</span>
                </label>
                <input
                  id="creatorNickname"
                  type="text"
                  value={formData.creatorNickname}
                  onChange={(e) => handleInputChange('creatorNickname', e.target.value)}
                  placeholder="e.g., John"
                  className={errors.creatorNickname ? 'error' : ''}
                  maxLength={20}
                />
                {errors.creatorNickname && <span className="error-message">{errors.creatorNickname}</span>}
                <span className="field-hint">This will be your display name in the draft</span>
              </div>
            </div>
          )}

          {currentStep === 2 && (
            <div className="wizard-step">
              <h2>Configure your draft</h2>
              <div className="form-group">
                <label htmlFor="participantCount">Number of Participants</label>
                <select
                  id="participantCount"
                  value={formData.participantCount}
                  onChange={(e) => handleInputChange('participantCount', parseInt(e.target.value))}
                  className="large-select"
                >
                  {Array.from({ length: 11 }, (_, i) => i + 2).map((num) => (
                    <option key={num} value={num}>
                      {num} Teams
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label htmlFor="totalRounds">Number of Rounds</label>
                <select
                  id="totalRounds"
                  value={formData.totalRounds}
                  onChange={(e) => handleInputChange('totalRounds', parseInt(e.target.value))}
                  className="large-select"
                >
                  {Array.from({ length: 7 }, (_, i) => i + 1).map((num) => (
                    <option key={num} value={num}>
                      {num} {num === 1 ? 'Round' : 'Rounds'}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label className="radio-group-label">Draft Type</label>
                <div className="radio-group">
                  <label className="radio-option">
                    <input
                      type="radio"
                      name="draftType"
                      checked={!formData.isSnakeDraft}
                      onChange={() => handleInputChange('isSnakeDraft', false)}
                    />
                    <span className="radio-label">
                      <strong>Linear</strong>
                      <span className="radio-description">All rounds: A→B→C→D</span>
                    </span>
                  </label>
                  <label className="radio-option disabled">
                    <input
                      type="radio"
                      name="draftType"
                      checked={formData.isSnakeDraft}
                      onChange={() => handleInputChange('isSnakeDraft', true)}
                      disabled={true}
                    />
                    <span className="radio-label">
                      <strong>Snake</strong>
                      <span className="radio-description">Coming soon</span>
                    </span>
                  </label>
                </div>
              </div>
            </div>
          )}

          {currentStep === 3 && (
            <div className="wizard-step">
              <h2>Secure your draft</h2>
              <div className="form-group">
                <label htmlFor="pin">
                  Draft PIN <span className="required">*</span>
                </label>
                <input
                  id="pin"
                  type="text"
                  value={formData.pin}
                  onChange={(e) => handleInputChange('pin', e.target.value.replace(/\D/g, '').slice(0, 4))}
                  placeholder="4-digit PIN"
                  className={`pin-input ${errors.pin ? 'error' : ''}`}
                  maxLength={4}
                  autoFocus
                />
                {errors.pin && <span className="error-message">{errors.pin}</span>}
                <span className="field-hint">Participants will need this PIN to join the draft</span>
              </div>

              <div className="draft-summary">
                <h3>Draft Summary</h3>
                <div className="summary-item">
                  <span className="summary-label">Draft Name:</span>
                  <span className="summary-value">{formData.draftName}</span>
                </div>
                <div className="summary-item">
                  <span className="summary-label">Your Nickname:</span>
                  <span className="summary-value">{formData.creatorNickname}</span>
                </div>
                <div className="summary-item">
                  <span className="summary-label">Participants:</span>
                  <span className="summary-value">{formData.participantCount} Teams</span>
                </div>
                <div className="summary-item">
                  <span className="summary-label">Rounds:</span>
                  <span className="summary-value">{formData.totalRounds}</span>
                </div>
                <div className="summary-item">
                  <span className="summary-label">Type:</span>
                  <span className="summary-value">{formData.isSnakeDraft ? 'Snake' : 'Linear'}</span>
                </div>
              </div>
            </div>
          )}

          {errors.submit && (
            <div className="error-banner">
              <span className="error-icon">⚠️</span>
              <span>{errors.submit}</span>
            </div>
          )}

          <div className="wizard-actions">
            {currentStep > 1 && (
              <button
                type="button"
                onClick={handleBack}
                className="btn btn-secondary"
                disabled={loading}
              >
                ← Back
              </button>
            )}
            {currentStep < totalSteps ? (
              <button
                type="button"
                onClick={handleNext}
                className="btn btn-primary"
              >
                Next →
              </button>
            ) : (
              <button
                type="button"
                onClick={handleSubmit}
                className="btn btn-primary"
                disabled={loading}
              >
                {loading ? (
                  <>
                    <span className="spinner"></span>
                    Creating...
                  </>
                ) : (
                  'Create Draft'
                )}
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default DraftSetupPage;
