# Contributing to Argus

## Security-First Development

Argus is a tactical oracle for elite football coaching. Security and correctness are non-negotiable.

### Before Submitting

1. **Run the Security Audit:**
   ```bash
   clojure -M -m argus.secure/audit
   ```
   Must return: `✅ Security Audit Passed`

2. **All Tests Pass:**
   ```bash
   clojure -M:test
   ```
   Zero failures, zero errors.

3. **No Dynamic Resolution:**
   ```bash
   grep -r "eval\|read-string\|resolve" src/
   ```
   Must return empty (except in documentation/comments).

## Code Standards
- **Pure Functions:** All detectors and simulators must be side-effect free
- **Immutable Data:** No mutable state except at system boundaries
- **Structural Sharing:** Verify via `(identical? old new)` tests
- **EDN Only:** No JSON, no XML—data is code

## Pull Request Template
- [ ] Security audit passes
- [ ] All tests pass
- [ ] New features have property-based tests
- [ ] Documentation updated
- [ ] No breaking changes (or clearly documented)
