# Toggles Documentation

This directory contains the static website for the Toggles project, hosted on GitHub Pages.

## Files

- `index.html` - Main homepage showcasing the Toggles apps and libraries
- `.gitignore` - Excludes temporary files from git

## Viewing Locally

To view the site locally, run:

```bash
cd docs
python3 -m http.server 8080
```

Then visit `http://localhost:8080` in your browser.

## GitHub Pages Setup

This site is configured to be served by GitHub Pages from the `docs/` directory. The site will be available at:
`https://erikeelde.github.io/toggles/`

To enable GitHub Pages:
1. Go to repository Settings
2. Navigate to Pages section  
3. Select "Deploy from a branch"
4. Choose `main` branch and `/docs` folder
5. Save the configuration