# Vite Migration Guide

## What Changed

### Build Tool
- ❌ **Removed**: Webpack 5
- ✅ **Added**: Vite 5

### Package Manager
- ❌ **Removed**: npm
- ✅ **Added**: pnpm (faster installs, better disk usage)

### Configuration Files
- ❌ **Removed**: `webpack.config.js`
- ✅ **Added**: `vite.config.ts`
- ✅ **Added**: `.npmrc` (pnpm configuration)
- ✅ **Updated**: `tsconfig.json` (Vite-compatible)
- ✅ **Added**: `tsconfig.node.json` (for Vite config)

### File Structure
- ✅ **Moved**: `src/index.html` → `index.html` (root level, Vite requirement)

### Dependencies Removed
All Webpack-related packages:
- webpack, webpack-cli, webpack-dev-server
- ts-loader, css-loader, sass-loader, style-loader, postcss-loader
- html-webpack-plugin, copy-webpack-plugin
- file-loader, url-loader
- @svgr/webpack
- node-sass

### Dependencies Added
Vite ecosystem:
- vite
- @vitejs/plugin-react
- vite-plugin-svgr
- sass (modern replacement for node-sass)

## Performance Improvements

### Development Server
- **Webpack**: 10-30 seconds startup
- **Vite**: ~200ms startup ⚡

### Hot Module Replacement (HMR)
- **Webpack**: 1-3 seconds per change
- **Vite**: Instant (<100ms) 🚀

### Production Build
- **Webpack**: 30-60 seconds
- **Vite**: 5-10 seconds 📦

### Dependency Installation
- **npm**: ~30-60 seconds
- **pnpm**: ~10-15 seconds 🎯

## Scripts Changed

### Old (npm + Webpack)
```bash
npm install
npm start        # Dev server
npm run build    # Production build
```

### New (pnpm + Vite)
```bash
pnpm install
pnpm dev         # Dev server
pnpm build       # Production build
pnpm preview     # Preview production build
```

## Import Changes

### SVG Imports
No changes needed! `vite-plugin-svgr` provides the same API as `@svgr/webpack`:

```tsx
import { ReactComponent as Logo } from './logo.svg';
```

### CSS/SCSS Imports
No changes needed! Vite handles these natively:

```tsx
import './styles.scss';
```

### Asset Imports
No changes needed! Vite handles images, fonts, etc. automatically:

```tsx
import logo from './logo.png';
```

## TypeScript Configuration

### Module System
- **Old**: CommonJS (`module: "commonjs"`)
- **New**: ESNext (`module: "ESNext"`)

### JSX Transform
- **Old**: `jsx: "react"` (requires `import React`)
- **New**: `jsx: "react-jsx"` (automatic JSX runtime)

This means you can remove `import React from 'react'` from files that only use JSX!

## Proxy Configuration

API proxy is configured in `vite.config.ts`:

```ts
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
}
```

## Path Aliases

Path aliases work the same way:

```ts
// vite.config.ts
resolve: {
  alias: {
    '@components': path.resolve(__dirname, './src/components'),
  },
}
```

## Troubleshooting

### Issue: Module not found
**Solution**: Make sure to use `.tsx` or `.ts` extensions in imports when needed.

### Issue: pnpm not found
**Solution**: Install pnpm globally: `npm install -g pnpm`

### Issue: Build fails with type errors
**Solution**: Run `pnpm install` to ensure all type definitions are installed.

### Issue: HMR not working
**Solution**: Make sure you're using `pnpm dev` and not `pnpm start`.

## Gradle Integration

The Gradle build has been updated to use pnpm:

```gradle
task clientBuild(type: PnpmTask, dependsOn: pnpmInstall) {
    args = ['run', 'build']
}
```

This means `./gradlew build` will automatically:
1. Install pnpm
2. Run `pnpm install`
3. Run `pnpm build`
4. Copy build output to Spring Boot resources

## Next Steps

1. Delete `node_modules` and `package-lock.json` if they exist
2. Run `pnpm install` to create `pnpm-lock.yaml`
3. Run `pnpm dev` to start the dev server
4. Enjoy the speed! ⚡
