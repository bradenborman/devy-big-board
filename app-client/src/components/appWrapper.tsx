import React from 'react';
import { BrowserRouter, Routes, Route, useParams } from 'react-router-dom';
import MainComponent from './mainComponent';
import ConsentBanner from './ConsentBanner';
import CompletedDraft from './CompletedDraft';
import LeagueFiltersPage from './LeagueFiltersPage';

const AppWrapper: React.FC = () => {
    // useEffect(() => {
    //     if (
    //         window.location.protocol === 'http:' &&
    //         window.location.hostname !== 'localhost'
    //     ) {
    //         window.location.href = window.location.href.replace('http:', 'https:');
    //     }
    // }, []);

    return (
        <BrowserRouter>
            <ConsentBanner />
            <Routes>
                <Route path="/" element={
                    <div className="app-wrapper">
                        <MainComponent />
                    </div>
                } />
                <Route path="/draft/:uuid" element={<DraftRoute />} />
                <Route path="/league-filters" element={<LeagueFiltersPage />} />
            </Routes>
        </BrowserRouter>
    );
};

const DraftRoute: React.FC = () => {
    const { uuid } = useParams<{ uuid: string }>();
    if (!uuid) return <div>Invalid Draft</div>;
    return <CompletedDraft uuid={uuid} />
};

export default AppWrapper;